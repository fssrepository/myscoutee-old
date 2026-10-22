#!/usr/bin/env python3
"""Restore a saved GitHub Project without the original chat history.

Dry-run by default. Requires Python 3 standard library only.
GITHUB_PROJECT_TOKEN: project-write token. GITHUB_TOKEN is a fallback.
GITHUB_REPOSITORY_TOKEN: optional issue-write token, only when recreating issues.
"""
import argparse,json,os,pathlib,urllib.request,urllib.error,time,hashlib

def api(path,payload=None,method=None,repository=False):
 token=os.getenv('GITHUB_REPOSITORY_TOKEN') if repository else os.getenv('GITHUB_PROJECT_TOKEN')
 token=token or os.getenv('GITHUB_TOKEN')
 if not token:raise SystemExit('Set GITHUB_PROJECT_TOKEN (or GITHUB_TOKEN) in your shell; do not put credentials in this repository.')
 req=urllib.request.Request('https://api.github.com/'+path,data=json.dumps(payload).encode() if payload is not None else None,method=method,headers={'Authorization':'Bearer '+token,'Content-Type':'application/json','Accept':'application/vnd.github+json','User-Agent':'myscoutee-project-restore','X-GitHub-Api-Version':'2022-11-28'})
 for attempt in range(4):
  try:
   with urllib.request.urlopen(req,timeout=45) as response:return json.load(response)
  except urllib.error.HTTPError as e:
   if attempt==3 or not (e.code==429 or (e.code==422 and method=='PATCH')):raise
   time.sleep(2*(attempt+1))
def gql(query,variables=None,repository=False):
 result=api('graphql',{'query':query,'variables':variables or {}},repository=repository)
 if result.get('errors'):raise RuntimeError(json.dumps(result['errors']))
 return result['data']
def validate(snapshot):
 assert snapshot['schema_version']==1,'Unsupported snapshot version'
 assert snapshot['items'],'Empty snapshot'
 ids=[i['task_id'] for i in snapshot['items']];assert len(ids)==len(set(ids)),'Duplicate task IDs'
 fields={f['name']:f for f in snapshot['fields']}
 for item in snapshot['items']:
  assert item['title'] and item['body'] and item['state'] in ['OPEN','CLOSED']
  assert all(name in fields for name in item['values']),'Unknown field name'
 return fields

def main():
 ap=argparse.ArgumentParser(description=__doc__);ap.add_argument('--snapshot',type=pathlib.Path,default=pathlib.Path(__file__).with_name('project.json'));ap.add_argument('--apply',action='store_true',help='Create a new Project or resume the saved restoration');ap.add_argument('--state',type=pathlib.Path,help='Local progress file, never committed');ap.add_argument('--recreate-missing-issues',action='store_true',help='Create missing issues using the optional repository-write credential')
 args=ap.parse_args();snap=json.loads(args.snapshot.read_text());validate(snap)
 checksum=args.snapshot.with_name('SHA256SUMS')
 if checksum.exists():
  for line in checksum.read_text().splitlines():
   expected,name=line.split(None,1);candidate=checksum.parent/name.strip()
   assert candidate.is_file(),f'Missing snapshot file: {name}'
   assert hashlib.sha256(candidate.read_bytes()).hexdigest()==expected,f'Snapshot checksum mismatch: {name}'
 print(f"Validated {len(snap['items'])} tasks, {len(snap['fields'])} fields, {len(snap['views'])} views.")
 if not args.apply:
  print('Dry run: no network requests or GitHub changes. Add --apply to restore.');return
 statepath=args.state or pathlib.Path.home()/'.local/state/project-history-restore'/(hashlib.sha256(args.snapshot.read_bytes()).hexdigest()[:16]+'.json');statepath.parent.mkdir(parents=True,exist_ok=True)
 state=json.loads(statepath.read_text()) if statepath.exists() else {'issues':{},'items':{},'complete':[],'views':[]}
 digest=hashlib.sha256(args.snapshot.read_bytes()).hexdigest()
 if state.get('snapshot_sha256') and state['snapshot_sha256']!=digest:raise SystemExit('This state belongs to a different snapshot. Use a different --state path.')
 state['snapshot_sha256']=digest
 def save():
  statepath.write_text(json.dumps(state,indent=2)+'\n');statepath.chmod(0o600)
 if 'project_id' not in state:
  owner=snap['project']['owner'];identity=gql('query($login:String!){repositoryOwner(login:$login){id}}',{'login':owner})['repositoryOwner']
  p=gql('mutation($i:CreateProjectV2Input!){createProjectV2(input:$i){projectV2{id url}}}',{'i':{'ownerId':identity['id'],'title':snap['project']['title']}})['createProjectV2']['projectV2'];state['project_id']=p['id'];state['url']=p['url'];save()
 pid=state['project_id']
 gql('mutation($i:UpdateProjectV2Input!){updateProjectV2(input:$i){projectV2{id}}}',{'i':{'projectId':pid,'public':snap['project']['public'],'shortDescription':snap['project']['shortDescription'],'readme':snap['project']['readme']}})
 live=gql('query($id:ID!){node(id:$id){...on ProjectV2{fields(first:50){nodes{...on ProjectV2Field{id name dataType} ...on ProjectV2SingleSelectField{id name options{id name}}}}}}}',{'id':pid})['node']['fields']['nodes'];fields={f['name']:f for f in live if f.get('name')}
 for f in snap['fields']:
  if f['name'] in fields:continue
  inp={'projectId':pid,'name':f['name'],'dataType':f['dataType']}
  if f['dataType']=='SINGLE_SELECT':inp['singleSelectOptions']=[{'name':o['name'],'color':o.get('color','GRAY'),'description':o.get('description','')} for o in f['options']]
  got=gql('mutation($i:CreateProjectV2FieldInput!){createProjectV2Field(input:$i){projectV2Field{...on ProjectV2Field{id name dataType} ...on ProjectV2SingleSelectField{id name options{id name}}}}}',{'i':inp})['createProjectV2Field']['projectV2Field'];fields[f['name']]=got
 for item in snap['items']:
  key=item['task_id']
  if key in state['complete']:continue
  if key not in state['issues']:
   ref=item['issue'];path='repos/'+ref['repository']+'/issues/'+str(ref['number'])
   try:issue=api(path)
   except urllib.error.HTTPError as e:
    if e.code!=404 or not args.recreate_missing_issues:raise
    issue=api('repos/'+ref['repository']+'/issues',{'title':item['title'],'body':item['body']},repository=True)
    if item['state']=='CLOSED':api('repos/'+ref['repository']+'/issues/'+str(issue['number']),{'state':'closed','state_reason':'completed'},method='PATCH',repository=True)
   state['issues'][key]={'node_id':issue['node_id'],'url':issue['html_url']};save()
  if key not in state['items']:
   result=gql('mutation($i:AddProjectV2ItemByIdInput!){addProjectV2ItemById(input:$i){item{id}}}',{'i':{'projectId':pid,'contentId':state['issues'][key]['node_id']}});state['items'][key]=result['addProjectV2ItemById']['item']['id'];save()
  inputs=[]
  for name,value in item['values'].items():
   f=fields[name];typ=next(x['dataType'] for x in snap['fields'] if x['name']==name)
   if typ=='SINGLE_SELECT':v={'singleSelectOptionId':next(o['id'] for o in f['options'] if o['name']==value)}
   else:v={ {'NUMBER':'number','DATE':'date','TEXT':'text'}[typ]:value}
   inputs.append({'projectId':pid,'itemId':state['items'][key],'fieldId':f['id'],'value':v})
  decl=','.join('$i'+str(i)+':UpdateProjectV2ItemFieldValueInput!' for i in range(len(inputs)));body=' '.join('m'+str(i)+':updateProjectV2ItemFieldValue(input:$i'+str(i)+'){projectV2Item{id}}' for i in range(len(inputs)))
  gql('mutation('+decl+'){'+body+'}',{'i'+str(i):v for i,v in enumerate(inputs)});state['complete'].append(key);save();print('Restored',key,flush=True)
 liveviews=gql('query($id:ID!){node(id:$id){...on ProjectV2{views(first:30){nodes{id name}}}}}',{'id':pid})['node']['views']['nodes']
 for v in snap['views']:
  if v['name'] in state['views']:continue
  config={'visibleFieldIds':[fields[n]['id'] for n in v['visibleFields']]};found=next((x for x in liveviews if x['name']==v['name']),None)
  if not found and not state['views'] and liveviews:found=liveviews[0]
  if found:gql('mutation($i:UpdateProjectV2ViewInput!){updateProjectV2View(input:$i){projectV2View{id}}}',{'i':{'viewId':found['id'],'name':v['name'],'layout':v['layout'],'filter':v.get('filter',''),'configuration':config}})
  else:gql('mutation($i:CreateProjectV2ViewInput!){createProjectV2View(input:$i){projectV2View{id}}}',{'i':{'projectId':pid,'name':v['name'],'layout':v['layout'],'configuration':config}})
  state['views'].append(v['name']);save()
 # Repository associations use the repository credential; the public Project is readable there.
 repositories=snap['project'].get('repositories',[])
 if repositories and (os.getenv('GITHUB_REPOSITORY_TOKEN') or os.getenv('GITHUB_TOKEN')):
  state.setdefault('linked_repositories',[])
  for fullname in repositories:
   if fullname in state['linked_repositories']:continue
   owner,name=fullname.split('/',1)
   repo=gql('query($owner:String!,$name:String!){repository(owner:$owner,name:$name){id}}',{'owner':owner,'name':name},repository=True)['repository']
   gql('mutation($p:ID!,$r:ID!){linkProjectV2ToRepository(input:{projectId:$p,repositoryId:$r}){repository{id}}}',{'p':pid,'r':repo['id']},repository=True)
   state['linked_repositories'].append(fullname);save();print('Linked repository',fullname,flush=True)
 elif repositories:
  print('Repository links are saved in the snapshot. Set GITHUB_REPOSITORY_TOKEN and rerun --apply to restore them too.')
 print('Restored project:',state['url'])
 print('Existing issues and source commits were preserved. Recreated missing issues can receive new issue numbers; stable task IDs are retained.')
if __name__=='__main__':main()
