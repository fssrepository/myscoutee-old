export interface ILazy {
  onEnter(alias, data?: any);
  onLeave();
}

export interface Progress {
  mode: string;
  value: string;
  color: string;
}