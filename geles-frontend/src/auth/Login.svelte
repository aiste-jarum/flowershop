<script>
  import { navigate } from "svelte-routing";
  import Input from "../Input.svelte";
  import { isAxiosError } from "../util";
  import axios from "axios";
  import { user } from "../stores";
  import { getContext } from "svelte";
  import { notificationContextKey } from "../contexts";

  const { loading, success, error } = getContext<AppNotificationContext>(
    notificationContextKey
  );

  let loginFields = {
    username: "",
    password: ""
  };

  async function handleSubmit() {
    try {
      await loading(
        "Prisijungiama...",
        (async () => {
          await axios.post("/auth/login/", loginFields, {
            withCredentials: true
          });
          const response = await axios.get<User>("/users/", {
            withCredentials: true
          });
          user.set(response.data);
        })()
      );
      success("Prisijungta");
      navigate("/");
    } catch (e) {
      if (isAxiosError(e)) {
        if (e.response) {
          if (e.response.status === 400) {
            error("Neteisingas vartotojo vardas ar slaptažodis");
          } else {
            error("Klaida prisijungiant");
          }
        }
      }
    }
  }
</script>

<div class="pagecontent">
  <h2>Prisijungti</h2>
  <form
    on:submit={e => {
      e.preventDefault();
      handleSubmit();
    }}
  >
    <Input
      label="Vartotojo vardas"
      bind:value={loginFields.username}
      name="username"
      type="text"
      autocomplete="nickname"
    /><br /><br />
    <Input
      label="Slaptažodis"
      bind:value={loginFields.password}
      name="password"
      type="password"
      autocomplete="current-password"
    /><br /><br />
    <button class="button">Prisijungti</button>
  </form>
</div>

<style>
  .button {
    margin-left: 0;
  }
</style>
