import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MsList } from 'src/lib';
import {
  BasketComponent,
  CarFormComponent,
  EventFilterComponent,
  EventFormComponent,
  FeedbackFormComponent,
  PanelComponent,
  ProfileComponent,
  QrcodeComponent,
  SettingComponent,
  RewardComponent,
  GroupFormComponent,
  SchoolComponent,
  ProfileStatusComponent
} from './components';

// filter, add, attach
const routes: Routes = [
  { path: '', redirectTo: 'activity', pathMatch: 'full' },
  {
    path: 'activity',
    data: {
      reuse: true,
      icon: 'notifications',
    },
    children: [
      { path: '', redirectTo: 'channels', pathMatch: 'full' },
      {
        path: 'channels',
        component: MsList,
        data: {
          reuse: true,
          icon: 'chat',
          actions: [
          ],
          event: true, //temporary
          animation: 'One',
          type: 'chat'
        },
      },
      {
        path: 'channels/:id',
        data: {
          dialog: true,
        },
        children: [
          { path: '', redirectTo: 'items', pathMatch: 'full' },
          {
            path: 'items',
            component: MsList,
            data: {
              reuse: true,
              actions: [
              ],
              animation: 'One1',
              type: 'chat',
              mqtt: true
            },
          }
        ]
      },
      {
        path: 'invitations',
        component: MsList,
        data: {
          reuse: true,
          icon: 'mail',
          event: true, //temporary
          animation: 'Two',
        },
      },
      {
        path: 'invitations/:id',
        data: {
          icon: 'insert_invitation',
          dialog: true,
        },
        children: [
          { path: '', redirectTo: 'items', pathMatch: 'full' },
          {
            path: 'items',
            component: MsList,
            data: {
              reuse: true,
              icon: 'list',
              event: true, //temporary
              animation: 'Two1',
            },
          },
          {
            path: 'members',
            component: MsList,
            data: {
              reuse: true,
              icon: 'groups',
              actions: [
              ],
            },
          },
          {
            path: 'feedbacks',
            component: MsList,
            data: {
              reuse: true,
              icon: 'feedback_ext',
            },
          },
        ],
      },
    ],
  },
  {
    path: 'games',
    data: {
      reuse: true,
      icon: 'favorite',
    },
    children: [
      { path: '', redirectTo: 'rate_none', pathMatch: 'full' },
      {
        path: 'rate_none',
        component: MsList,
        data: {
          reuse: true,
          icon: 'star',
          double: true,
          group: 'dist',
          filter: true
        },
      },
      {
        path: 'rate_give',
        component: MsList,
        data: {
          reuse: true,
          icon: 'rate_ext_give',
          double: true,
        },
      },
      {
        path: 'rate_receive',
        component: MsList,
        data: {
          reuse: true,
          icon: 'rate_ext_receive',
          double: true,
        },
      },
      {
        path: 'rate_both',
        component: MsList,
        data: {
          reuse: true,
          icon: 'rate_ext_both',
          double: true,
        },
      },
      {
        //path: 'rate_double/:id',
        path: 'rate_double',
        component: MsList,
        data: {
          reuse: true,
          icon: 'groups',
          double: true,
          group: 'dist',
          filter: true,
          actions: [
            {
              component: BasketComponent,
              type: 'add',
              icon: 'group_add',
            },
          ]
        }
      },
    ],
  },
  {
    path: 'user',
    data: {
      reuse: true,
      icon: 'person',
      //type: 'P',
    },
    children: [
      { path: '', redirectTo: 'profile', pathMatch: 'full' },
      {
        path: 'profile',
        component: PanelComponent,
        data: {
          reuse: true,
          icon: 'profile_ext',
          actions: [
            {
              type: 'route',
              icon: 'school',
              url: '../schools',
            },
            {
              component: RewardComponent,
              type: 'share',
              icon: 'send',
            },
            {
              component: RewardComponent,
              type: 'reward',
              icon: 'military_tech',
            },
            {
              component: SettingComponent,
              type: 'logout',
              icon: 'logout',
              color: 'warn',
            },
            { component: ProfileComponent, type: 'edit', icon: 'edit' },
          ],
        },
      },
      {
        path: 'schools',
        component: MsList,
        data: {
          reuse: true,
          icon: 'school',
          actions: [
            {
              component: SchoolComponent,
              type: 'add',
              icon: 'add',
            },
            {
              component: SchoolComponent,
              type: 'edit',
            },
          ],
          event: true, //temporary
        },
      },
      {
        path: 'cars',
        component: MsList,
        data: {
          reuse: true,
          icon: 'car_ext',
          actions: [
            {
              component: CarFormComponent,
              type: 'add',
              icon: 'add',
            },
            {
              component: CarFormComponent,
              type: 'edit',
            },
          ],
          event: true, //temporary
        },
      },
      //groups will be added onto logout screen
      //create/leave/join group and login into group(switch) on the panel
      //no settings.component for logout

      // it's for group invite, when you are going to restructure a team (at the top menu bar)
      // you can select event created by organizer or you can organize your own by group
      // admin groups has statistics tab - who is the less popular, why? who is the most populat why?
      // why is when you like by category for people
      // show optimal groups (the size of the group is configurable)
      {
        path: 'groups',
        component: MsList,
        data: {
          reuse: true,
          icon: 'groups',
          actions: [
            {
              component: GroupFormComponent,
              type: 'add',
              icon: 'group_add',
            },
            {
              type: 'route', //clone a group or promotional event
              icon: 'explore',
              // only profiles - members url part will be replaced by profiles
              url: 'dating/user/groups/recommendations', // backend call - not rate_met, filter out already added members
            },
            {
              component: GroupFormComponent,
              type: 'edit',
            },
            {
              component: GroupFormComponent,
              type: 'share',
            },
          ],
        },
      },
      {
        path: 'groups/recommendations',
        data: {
          dialog: true,
        },
        children: [
          {
            path: '',
            component: MsList,
            data: {
              //icon: 'groups',
              reuse: true,
              // icon: 'list',
              actions: [
              ],
            },
          },
        ]
      },
      {
        path: 'groups/:id',
        data: {
          icon: 'insert_invitation',
          dialog: true,
        },
        children: [
          { path: '', redirectTo: 'profiles', pathMatch: 'full' },
          {
            path: 'profiles',
            component: MsList,
            data: {
              icon: 'groups',
              reuse: true,
              // icon: 'list',
              actions: [
                {
                  component: ProfileStatusComponent,
                  type: 'route',
                  icon: 'group_add',
                  url: 'dating/user/groups/profiles' //?excludeGroupId=:id list out profiles in parent group which is not assigned to this sub group
                },
                {
                  component: ProfileStatusComponent,
                  type: 'edit',
                },
              ],
            },
          },
          {
            path: 'events',
            component: MsList,
            data: {
              reuse: true,
              icon: 'local_activity',
              actions: [
                {
                  component: EventFormComponent,
                  type: 'add',
                  icon: 'add',
                },
                {
                  // component: EventFormComponent,
                  component: QrcodeComponent,
                  type: 'verify',
                },
                {
                  // component: EventFormComponent,
                  component: QrcodeComponent,
                  type: 'code',
                },
              ],
              multiple: true,
              event: true, //temporary
              animation: 'One',
            },
          },
          {
            path: 'events/:id',
            data: {
              icon: 'local_activity',
              dialog: true,
            },
            children: [
              { path: '', redirectTo: 'items', pathMatch: 'full' },
              {
                path: 'items',
                component: MsList,
                data: {
                  reuse: true,
                  actions: [
                    {
                      component: EventFormComponent,
                      type: 'add',
                      icon: 'add',
                    },
                    {
                      component: EventFormComponent,
                      type: 'edit',
                    },
                  ],
                  event: true, //temporary
                  animation: 'One1',
                },
              },
            ],
          },
        ],
      },
    ],
  },
  {
    path: 'history',
    data: {
      reuse: true,
      icon: 'event',
    },
    children: [
      { path: '', redirectTo: 'events', pathMatch: 'full' },
      {
        path: 'events',
        component: MsList,
        data: {
          reuse: true,
          actions: [
            {
              component: EventFormComponent,
              type: 'add',
              icon: 'add',
            },
            {
              type: 'route', //clone a group or promotional event
              icon: 'explore',
              // only profiles - members url part will be replaced by profiles
              url: 'dating/history/events/recommendations', // backend call - not rate_met, filter out already added members
            },
            {
              // component: EventFormComponent,
              component: QrcodeComponent,
              type: 'verify',
            },
            {
              // component: EventFormComponent,
              component: QrcodeComponent,
              type: 'code',
            },
            {
              component: EventFormComponent,
              type: 'edit',
            },
          ],
          multiple: true,
          event: true, //temporary
          animation: 'One',
          group: 'date',
        }
      },
      {
        path: 'events/recommendations',
        data: {
          dialog: true,
        },
        children: [
          {
            path: '',
            component: MsList,
            data: {
              //icon: 'groups',
              reuse: true,
              // icon: 'list',
              actions: [
              ],
            },
          },
        ]
      },
      {
        path: 'events/:id',
        data: {
          icon: 'insert_invitation',
          dialog: true,
        },
        children: [
          { path: '', redirectTo: 'items', pathMatch: 'full' },
          {
            path: 'items',
            component: MsList,
            data: {
              reuse: true,
              icon: 'list',
              actions: [
                {
                  component: EventFormComponent,
                  type: 'add',
                  icon: 'add',
                },
                {
                  component: EventFormComponent,
                  type: 'edit',
                },
              ],
              event: true, //temporary
              animation: 'One1',
            },
          },
          {
            path: 'items/:id',
            data: {
              icon: 'insert_invitation',
              dialog: true,
            },
            children: [
              { path: '', redirectTo: 'members', pathMatch: 'full' },
              {
                path: 'members',
                component: MsList,
                data: {
                  reuse: true,
                  animation: 'Two1',
                },
              },
            ],
          },
          {
            path: 'members',
            component: MsList,
            data: {
              reuse: true,
              icon: 'groups',
              actions: [
                {
                  component: BasketComponent,
                  type: 'add',
                  icon: 'group_add',
                  multiple: true,
                  // only profiles - members url part will be replaced by profiles
                  url: '/games/rate_both', // backend call - not rate_met, filter out already added members
                },
              ],
            },
          },
          {
            path: 'feedbacks',
            component: MsList,
            data: {
              reuse: true,
              icon: 'feedback_ext',
              actions: [
                {
                  component: FeedbackFormComponent,
                  type: 'add',
                  icon: 'add',
                },
                {
                  component: FeedbackFormComponent,
                  type: 'edit',
                },
              ],
            },
          },
        ],
      },
    ]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DatingRoutingModule { }
