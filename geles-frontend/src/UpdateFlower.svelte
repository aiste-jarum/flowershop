<script>
  import { isAxiosError } from "./util";
  import { navigate } from "svelte-routing";
  import { getContext, onMount } from "svelte";
  import axios from "axios";

  import Input from "./Input.svelte";
  import { notificationContextKey } from "./contexts";

  const { loading, success, error } = getContext<AppNotificationContext>(
    notificationContextKey
  );

  let flower: Omit<Flower, "id"> = {
    name: "",
    price: 0,
    description: "",
    daysToExpire: 0,
    version: 0
  };

  export let id: string;

  let errors: string[] = [];

  async function handleSubmit() {
    try {
      // Creates a loading notification, awaits the passed promise
      // and removes the notification after promise was completed
      await loading(
        "Redaguojama...",
        axios.put(`/flowers/${id}`, flower, {
          withCredentials: true
        })
      );
      success("Gėlė sėkmingai paredaguota");
      navigate("/");
    } catch (e) {
      if (isAxiosError(e)) {
        if (e.response) {
          if (e.response.status === 400) {
            errors = e.response.data.errors.map(
              error => `${error.field} ${error.defaultMessage}`
            );
            return; // Avoid showing error message
          } else if (e.response.status === 500) {
            errors = [`Internal server error: ${e.response.data.message}`];
          } else if (e.response.status === 409) {
            if (
              window.confirm(
                "Informacija buvo pakeista kito vartotojo. Spauskite „Gerai“, jei norite pakeisti esamus duomenis įvestais. Norėdami atsinaujinti duomenis, perkraukite puslapį."
              )
            ) {
              const newFlower = await getFlower();
              flower.version = newFlower.version;
              handleSubmit();
            }
            return;
          }
        }
      }
      error("Klaida redaguojant gėlę");
    }
  }

  onMount(async () => {
    flower = await getFlower();
  });

  async function getFlower() {
    return (
      await axios.get<Flower>(`/flowers/${id}`, { withCredentials: true })
    ).data;
  }
</script>

<div class="pagecontent">
  <h2>Redaguoti gėlę</h2>
  <form
    on:submit={e => {
      e.preventDefault();
      handleSubmit();
    }}
  >
    <Input
      label="Pavadinimas"
      bind:value={flower.name}
      type="text"
      name="name"
    /><br />
    <Input
      label="Kaina"
      bind:value={flower.price}
      type="number"
      min={0}
      step="0.01"
      name="price"
    /><br />
    <Input
      label="Aprašymas"
      bind:value={flower.description}
      type="text"
      name="description"
    /><br />
    <button class="button save">Redaguoti</button>
    {#each errors as error}
      <p class="error">
        <i class="mdi mdi-alert-circle" />
        {error.slice(0, 1).toUpperCase()}{error.slice(1)}
      </p>
    {/each}
  </form>
</div>

<style>
  .error {
    color: red;
    display: flex;
    align-items: center;
  }

  .error .mdi {
    font-size: 24px;
    margin-right: 8px;
  }
  .button {
    margin-top: 10px;
    margin-left: 0;
  }
</style>
