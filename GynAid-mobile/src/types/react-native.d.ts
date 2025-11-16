declare var __DEV__: boolean;

interface Console {
  error(message?: any, ...optionalParams: any[]): void;
}

declare const console: Console;