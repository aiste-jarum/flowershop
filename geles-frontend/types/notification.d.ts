interface AppNotification {
  id: number;
  text: string;
  type: AppNotificationType;
  loading?: boolean;
}

interface AppNotificationContext {
  addNotification(
    text: string,
    type: AppNotificationType,
    timeout?: number
  ): number;
  addLoadingNotification(text: string, promise: Promise<T>): Promise<T>;
  removeNotification(id: number): boolean;
  loading(text: string, promise: Promise<T>): Promise<T>;
  success(text: string): number;
  error(text: string): number;
  warning(text: string): number;
  info(text: string): number;
}
