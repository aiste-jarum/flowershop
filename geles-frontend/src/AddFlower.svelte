<script>
  import { isAxiosError } from "./util";
  import { navigate } from "svelte-routing";
  import axios from "axios";

  import Input from "./Input.svelte";
  import { getContext } from "svelte";
  import { notificationContextKey } from "./contexts";
  import { AppNotificationType } from "./enums";

  const { loading, success, error } = getContext<AppNotificationContext>(
    notificationContextKey
  );

  let flower: Omit<Flower, "id" | "favorite"> = {
    name: "",
    price: 0,
    description: "",
    daysToExpire: 1,
    photo: ""
  };

  let errors: string[] = [];
  let files: FileList;

  async function upload() {
    const formData = new FormData();
    formData.append("file", files[0]);
    try {
      await axios.post("/files/", formData, {
        headers: { "Content-Type": "multipart/form-data" }
      });
    } catch (e) {
      if (isAxiosError(e)) {
        if (e.response) {
          if (e.response.status === 500) {
            errors = [`Internal server error: ${e.response.data.message}`];
          }
        }
      }
      error("Nepavyko pridėti nuotraukos");
    }
  }

  async function handleSubmit() {
    const hasFile = files && !!files[0];
    if (hasFile) {
      if (!files[0].type.match(/image\/.*/)) {
        errors = ["Pasirinkite tinkamą nuotraukos formatą"];
        return;
      }
      if (files[0].size > 1024 * 1024) {
        errors = ["Nuotrauka per didelė, daugiausiai 1 MB"];
        return;
      }
      flower.photo = files[0].name;
    }
    try {
      await loading(
        "Pridedama...",
        (async () => {
          await axios.post("/flowers/", flower, {
            withCredentials: true
          });
          if (hasFile) {
            await upload();
          }
        })()
      );
      success("Gėlė sėkmingai pridėta");
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
          }
        }
      }
      error("Klaida pridedant gėlę");
    }
  }
</script>

<div class="pagecontent">
  <h2>Pridėti gėlę</h2>
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
    <label for="fileUpload">Nuotrauka</label>
    <br />
    <div class="container">
      <div class="button-wrap">
        <button
          class="button add"
          type="button"
          on:click={() => {
            document.getElementById("fileUpload")?.click();
          }}>Pridėti</button
        >
        <input
          id="fileUpload"
          type="file"
          multiple={false}
          bind:files
          accept="image/*"
          style="display:none;"
        />
        {#if !files}
          Nuotrauka nepasirinkta
        {:else}
          Nuotrauka: {files[0].name}
        {/if}
      </div>
    </div>
    <br />
    <button class="button save">Sukurti</button>
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

  .container {
    display: flex;
    align-items: flex-start;
    justify-content: flex-start;
    width: 100%;
  }

  .button {
    margin-top: 10px;
    margin-left: 0;
    width: 85px;
  }
</style>
