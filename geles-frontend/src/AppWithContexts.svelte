<script>
  import { Router } from "svelte-routing";
  import App from "./App.svelte";

  import NotificationContext from "./NotificationContext.svelte";

  export let url = "";
</script>

<NotificationContext>
  <Router {url}>
    <App />
  </Router>
</NotificationContext>
