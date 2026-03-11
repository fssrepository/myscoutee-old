export const img_f = 'assets/img';

enum REGEX {
  //email/month/date probably are not needed because of input type=email
  email = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$",
  password = '^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])(?=.{8,})',
  password_mid = '^(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))(?=.{6,})',
  month = '[0-9]{4}-(0[1-9]|1[012])',
  date = '[0-9]{4}-(0[1-9]|1[012])-(0[1-9]|1[0-9]|2[0-9]|3[01])',
  datetime = '[0-9]{4}-(0[1-9]|1[012])-(0[1-9]|1[0-9]|2[0-9]|3[01])[T][0-9]{2}:[0-9]{2}',
}

export var info = { isPwa: true };

export var isMobile = {
  Windows: function () {
    return /IEMobile/i.test(navigator.userAgent);
  },
  Android: function () {
    return /Android/i.test(navigator.userAgent);
  },
  BlackBerry: function () {
    return /BlackBerry/i.test(navigator.userAgent);
  },
  iOS: function () {
    return /iPhone|iPad|iPod/i.test(navigator.userAgent);
  },
  any: function () {
    return (
      isMobile.Android() ||
      isMobile.BlackBerry() ||
      isMobile.iOS() ||
      isMobile.Windows()
    );
  },
};

export var isFullScreenMode = {
  Android: function () {
    return (
      isMobile.Android() &&
      window.matchMedia('(display-mode: standalone)').matches
    );
  },
  iOS: function () {
    return isMobile.iOS() && (<any>window).navigator.standalone;
  },
  any: function () {
    return (
      !isMobile.any() || isFullScreenMode.Android() || isFullScreenMode.iOS()
    );
  },
};
