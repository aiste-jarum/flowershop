import axios from "axios";
import "svelte";

import AppWithContexts from "./AppWithContexts.svelte";

export const server_url = "http://localhost:8080";
axios.defaults.baseURL = server_url;
axios.defaults.withCredentials = true;

// Axios interceptor
axios.interceptors.response.use(
  response => {
    // Pass response on
    return response;
  },
  error => {
    // Pass error on
    return Promise.reject(error);
  }
);

var app = new AppWithContexts({
  target: document.body
});

export default app;

// Hot Module Replacement (HMR) - Remove this snippet to remove HMR.
// Learn more: https://www.snowpack.dev/concepts/hot-module-replacement
if (import.meta.hot) {
  import.meta.hot.accept();
  import.meta.hot.dispose(() => {
    app.$destroy();
  });
}
