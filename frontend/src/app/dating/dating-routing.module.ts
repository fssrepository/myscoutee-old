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
  PromotionFilterComponent,
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
          actions: [
          ],
          event: true, //temporary
          animation: 'One'
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
            },
          }
        ]
      },
      /*{
        path: 'events',
        component: MsList,
        data: {
          reuse: true,
          icon: 'event',
          actions: [
          ],
          multiple: true,
          event: true, //temporary
          animation: 'One',
          group: 'date',
        },
      },*/
      /*{
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
                  icon: 'add',
                  multiple: true,
                  // only profiles - members url part will be replaced by profiles
                  url: '/games/rate_met', // backend call - not rate_met, filter out already added members
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
      },*/
      /*{
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
              actions: [],
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
      {
        path: 'promotions',
        component: MsList,
        data: {
          reuse: true,
          icon: 'campaign',
        },
      },
      {
        path: 'promotions/:id',
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
              event: true, //temporary
              animation: 'One1',
            },
          },
        ],
      },*/
    ],
  },
  /*{
    path: 'recommendations',
    data: {
      reuse: true,
      icon: 'explore',
      animation: 'Three',
    },
    children: [
      { path: '', redirectTo: 'events', pathMatch: 'full' },
      {
        path: 'events',
        component: MsList,
        data: {
          reuse: true,
          icon: 'local_activity',
          event: true, //temporary
          animation: 'Three1',
        },
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
              ],
              event: true, //temporary
              animation: 'Three2',
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
                  component: EventFormComponent,
                  type: 'add',
                  icon: 'add',
                },
              ],
            },
          },
        ],
      },
      {
        path: 'promotions',
        component: MsList,
        data: {
          reuse: true,
          icon: 'campaign',
          // actions: [
          //   // Group-Filter has the common filter settings handling - this component will be removed
          //   {
          //     component: PromotionFilterComponent,
          //     type: 'filter',
          //     icon: 'search',
          //   },
          // ],
        },
      },
      {
        path: 'promotions/:id',
        data: {
          icon: 'insert_invitation',
          dialog: true,
        },
        children: [
          { path: '', redirectTo: 'events', pathMatch: 'full' },
          {
            path: 'events',
            component: MsList,
            data: {
              reuse: true,
              event: true, //temporary
              animation: 'One1',
              actions: [
                {
                  component: EventFormComponent,
                  type: 'clone',
                },
              ],
            },
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
                  event: true, //temporary
                  animation: 'One1',
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
        path: 'groups',
        component: MsList,
        data: {
          reuse: true,
          icon: 'groups',
          actions: [
            {
              type: 'join',
            },
          ],
        },
      },
    ],
  },*/
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
          icon: 'rate_ext_new',
          double: true,
          group: 'dist',
          filter: true
        },
      },
      {
        path: 'rate_double/:id',
        data: {
          icon: 'insert_invitation',
          dialog: true,
        },
        children: [
          { path: '', redirectTo: 'none', pathMatch: 'full' },
          {
            path: 'none',
            component: MsList,
            data: {
              reuse: true,
              icon: 'screen_share',
              animation: 'Four',
            },
          },
          {
            path: 'rated',
            component: MsList,
            data: {
              reuse: true,
              icon: 'rate_ext',
            },
          },
        ],
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
      /*{
        path: 'rate_met',
        component: MsList,
        data: {
          reuse: true,
          icon: 'history',
          double: true,
        },
        // filter button - hasonlosag vagy lokalizacio (ez ugyanaz amikor a resztvevoket valasztja ki egy esemenyhez)
        // basket component filter button
      },*/
      // ?state=active,pending,none
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
          //icon: 'profile_ext',
          actions: [
            /*{
              component: RewardComponent,
              type: 'share',
              icon: 'send',
            },
            {
              component: RewardComponent,
              type: 'reward',
              icon: 'military_tech',
            },*/
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
      /*{
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
      },*/
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DatingRoutingModule {}
