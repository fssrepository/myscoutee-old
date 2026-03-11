import {
  ActivatedRouteSnapshot,
  DetachedRouteHandle,
  RouteReuseStrategy
} from '@angular/router';

export function getUrl(route: ActivatedRouteSnapshot): string {
  if (route['_routerState']) {
    return route['_routerState'].url;
  }
}
export class CustomRouteReuseStrategy implements RouteReuseStrategy {
  private handlers: { [key: string]: DetachedRouteHandle } = {};

  shouldDetach(route: ActivatedRouteSnapshot): boolean {
    if (!route.routeConfig || route.routeConfig.children || route.routeConfig.loadChildren) {
      return false;
    }
    let shouldReuse = false;
    console.log(
      '[router-reuse] checking if this route should be re used or not',
      route
    );
    if (route.routeConfig.data) {
      route.routeConfig.data.reuse
        ? (shouldReuse = true)
        : (shouldReuse = false);
    }

    return shouldReuse;
  }

  store(
    route: ActivatedRouteSnapshot,
    handler: DetachedRouteHandle | null
  ): void {
    console.log('[router-reuse] storing handler');
    if (handler) {
      this.handlers[getUrl(route)] = handler;
    }
  }

  shouldAttach(route: ActivatedRouteSnapshot): boolean {
    return !!route.routeConfig && !!this.handlers[getUrl(route)];
  }

  retrieve(route: ActivatedRouteSnapshot): DetachedRouteHandle | null {
    if (!route.routeConfig || route.routeConfig.children || route.routeConfig.loadChildren) {
      return null;
    }
    return this.handlers[getUrl(route)]
      ? this.handlers[getUrl(route)]
      : null;
  }

  shouldReuseRoute(
    future: ActivatedRouteSnapshot,
    curr: ActivatedRouteSnapshot
  ): boolean {
    if (curr.routeConfig === null) {
      return true;
    }
    return future.data.reuse && future.routeConfig === curr.routeConfig;
  }
}
