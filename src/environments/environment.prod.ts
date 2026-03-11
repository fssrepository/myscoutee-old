import { appKeys } from '../secrets/keys';

export const environment = {
  production: true,
  mockUiData: false,
  disableFirebaseAuth: false,
  serverUrl: "http://localhost:8080/",
  firebase: appKeys.firebase,
  vapid: appKeys.vapid,
  groupTypes: {
    d: 'dating',
    b: 'business',
    t: 'job',
    a: 'admin',
  }
};
