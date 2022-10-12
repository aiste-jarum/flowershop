<script>
  import { getContext, onMount } from "svelte";
  import { server_url } from "./index";
  import axios from "axios";
  import { Link } from "svelte-routing";
  import Input from "./Input.svelte";
  import { notificationContextKey } from "./contexts";

  const { loading, success, error } = getContext<AppNotificationContext>(
    notificationContextKey
  );

  interface CartTemplate {
    id: number;
    name: string;
    flowersInCart: FlowerInCart[];
  }

  let cartErrors: string[] = [];

  let cart: Cart = {
    id: 0,
    flowersInCart: []
  };

  let cartTemplate: CartTemplate = {
    id: 0,
    name: "",
    flowersInCart: []
  };

  let flowers: Flower[] = [];
  let cartTemplates: CartTemplate[] = [];

  async function getCart() {
    const response = await axios.get<Cart>("/carts/", {
      withCredentials: true
    });
    cart = response.data;
    getFlowersInCart();
  }

  async function getFlowers() {
    // Download data from server
    const response = await axios.get("/flowers/");
    flowers = response.data;
  }

  async function getFlowersInCart() {
    // Download data from server
    const response = await axios.get(`/carts/flowers/${cart.id}`);
    cart.flowersInCart = mapFlowersInCart(response.data);
  }

  function mapFlowersInCart(data: any) {
    return data.map((flowerInCart: any) => {
      const flower = flowers.find(
        flower => flower.id === flowerInCart.flowerId
      );
      if (!flower) {
        return flowerInCart;
      }
      return {
        ...flowerInCart,
        name: flower.name,
        price: flower.price,
        sum: flower.price * flowerInCart.amount,
        photo: flower.photo
      };
    });
  }

  function updateSum(flower: FlowerInCart, newAmount: string) {
    const index = cart.flowersInCart.findIndex(
      flowerInCart => flowerInCart.id === flower.id
    );
    if (index === -1) {
      return;
    }
    cart.flowersInCart[index].sum = flower.price * +newAmount;
  }

  function handleDelete(fl: FlowerInCart) {
    let index = cart.flowersInCart.indexOf(fl);
    if (index !== -1) {
      cart.flowersInCart = [
        ...cart.flowersInCart.slice(0, index),
        ...cart.flowersInCart.slice(index + 1)
      ];
    }
  }

  async function handleUpdate() {
    try {
      await loading(
        "Išsaugoma...",
        (async () => {
          const response = await axios.put(
            `${server_url}/carts/${cart.id}`,
            cart
          );
          try {
            cart.flowersInCart = mapFlowersInCart(response.data.flowersInCart);
          } catch (e) {}
        })()
      );
      success("Pakeitimai išsaugoti");
    } catch (e) {
      error("Klaida išsaugant pakeitimus");
    }
  }

  async function getCartTemplates() {
    const response = await axios.get(`${server_url}/templates/`, {
      withCredentials: true
    });
    cartTemplates = response.data.map((cartTemplate: CartTemplate) => {
      cartTemplate.flowersInCart = mapFlowersInCart(cartTemplate.flowersInCart);
      return cartTemplate;
    });
  }

  async function handleSave() {
    if (cartTemplate.name.length > 0) {
      cartErrors = [];
      let template = {
        name: cartTemplate.name,
        flowersInCart: cart.flowersInCart
      };
      try {
        await loading(
          "Išsaugoma...",
          (async () => {
            await axios.post(`${server_url}/templates/`, template, {
              withCredentials: true
            });
            try {
              await getCartTemplates();
            } catch (e) {}
          })()
        );
        success("Krepšelis išsaugotas");
      } catch (e) {
        error("Klaida išsaugant krepšelį");
      }
    } else {
      cartErrors = ["Įrašykite šablono pavadinimą"];
    }
  }

  async function handleLoadTemplate(template: CartTemplate) {
    let flowersInTemplate =
      cartTemplates[cartTemplates.indexOf(template)].flowersInCart;

    let c = {
      id: cart.id,
      flowersInCart: flowersInTemplate
    };

    const response = await axios.put(`${server_url}/carts/${c.id}`, c);
    cart.flowersInCart = mapFlowersInCart(response.data.flowersInCart);
  }

  async function handleDeleteTemplate(template: CartTemplate) {
    try {
      if (
        window.confirm(`Ar tikrai norite ištrinti krepšelį ${template.name}?`)
      ) {
        await loading(
          "Ištrinama...",
          (async () => {
            await axios.delete(`${server_url}/templates/${template.id}`);
            try {
              await getCartTemplates();
            } catch (e) {}
          })()
        );
        success("Krepšelis ištrintas");
      }
    } catch (e) {
      error("Klaida ištrinant krepšelį");
    }
  }

  function calculateSum(template: CartTemplate) {
    let sum = 0;

    for (const flowerInTemplate of template.flowersInCart) {
      sum += flowerInTemplate.amount * flowerInTemplate.price;
    }
    return sum;
  }

  // Run code on component mount (once)
  onMount(async () => {
    await getFlowers();
    getCart();
    getCartTemplates();
  });
</script>

<div class="pagecontent">
  <div class="flowercart">
    <h2>Gėlių krepšelis</h2>
    {#if cart.flowersInCart.length > 0}
      <table>
        <tr>
          <th colspan="2">Gėlė</th>
          <th>Kiekis</th>
          <th>Vnt. kaina</th>
          <th>Suma</th>
          <th />
        </tr>
        {#each cart.flowersInCart as flowerInCart (flowerInCart.id)}
          <tr>
            <div class="imagecontainer">
              {#if flowerInCart.photo != null}
                <img
                  class="flower-list-item-photo"
                  src="{server_url}/files/{flowerInCart.photo}"
                  alt={flowerInCart.name}
                  width="80"
                  height="80"
                />
              {/if}
            </div>
            <td>{flowerInCart.name}</td>
            <td>
              <input
                class="numberinput"
                type="number"
                bind:value={flowerInCart.amount}
                min="1"
                max="100"
                size="5"
                on:input={e => {
                  updateSum(flowerInCart, e.currentTarget.value);
                }}
              />
            </td>
            <td class="number">{flowerInCart.price} €</td>
            <td class="number">{flowerInCart.sum?.toFixed(2)}€</td>
            <td>
              <button
                class="button delete"
                on:click={() => handleDelete(flowerInCart)}>Pašalinti</button
              >
            </td>
          </tr>
        {/each}
      </table>
    {:else}
      <p>Krepšelis tuščias!</p>
    {/if}
    <button class="button save" on:click={() => handleUpdate()}>
      Išsaugoti pakeitimus
    </button>
    {#if cart.flowersInCart.length > 0}
      <Link class="button next" to={`/order/${cart.id}`}>Užsakyti</Link>
      <div class="savetemplatecontainer">
        <Input
          style="display:inline-block;"
          label=""
          placeholder="Šablono pavadinimas"
          bind:value={cartTemplate.name}
        />
        <button class="button favorite" on:click={() => handleSave()}>
          Išsaugoti krepšelį ateičiai
        </button>
      </div>
    {/if}
    {#each cartErrors as error}
      <p class="error">
        <i class="mdi mdi-alert-circle" />
        {error.slice(0, 1).toUpperCase()}{error.slice(1)}
      </p>
    {/each}
  </div>
  <div class="carttemplate-list">
    <h2>Išsaugoti krepšeliai</h2>
    {#if cartTemplates.length > 0}
      <table class="template-table">
        <tr>
          <th>Krepšelis</th>
          <th>Gėlės</th>
          <th>Kiekis</th>
          <th>Suma</th>
          <th colspan="2" />
        </tr>
        {#each cartTemplates as template (template.id)}
          {#each template.flowersInCart as flowerInTemplate, i (flowerInTemplate.id)}
            <tr>
              {#if i === 0}
                <td
                  class="templatename"
                  rowspan={template.flowersInCart.length}
                >
                  {template.name}
                </td>
              {/if}
              <td>
                {flowerInTemplate.name}
              </td>
              <td>
                {flowerInTemplate.amount}
              </td>
              {#if i === 0}
                <td rowspan={template.flowersInCart.length}
                  >{calculateSum(template).toFixed(2)} €</td
                >
                <td rowspan={template.flowersInCart.length}>
                  <button
                    class="button add"
                    on:click={() => handleLoadTemplate(template)}
                    >Naudoti</button
                  >
                  <button
                    class="button delete"
                    on:click={() => handleDeleteTemplate(template)}
                    >Pašalinti</button
                  >
                </td>
              {/if}
            </tr>
          {/each}
        {/each}
      </table>
    {:else}
      <p>Nėra išsaugotų krepšelių!</p>
    {/if}
  </div>
</div>

<style>
  h2 {
    color: #000000;
    font-size: 24px;
    font-weight: 400;
  }

  table,
  th,
  td {
    border: 2px solid #8ebf42;
    border-collapse: collapse;
  }

  table {
    background-color: #d2ebb1d3;
    border: 4px solid #8ebf42;
    margin-bottom: 10px;
  }

  th,
  td {
    padding: 4px 8px;
  }

  td {
    text-align: center;
  }

  td.number {
    text-align: right;
  }

  td input {
    background-color: #e6f5d4;
  }

  img {
    margin: 8px;
    /* Weird bug with table cell height */
    margin-bottom: 4px;
  }

  .savetemplatecontainer {
    margin-top: 20px;
  }

  .flowercart {
    margin-bottom: 80px;
  }

  .templatename {
    text-align: left;
  }
  .error {
    color: red;
    display: flex;
    align-items: center;
  }

  .button {
    margin-top: 10px;
    margin-left: 0;
  }

  td > .button {
    margin-top: 4px;
  }

  .favorite {
    margin-left: 8px;
  }

  .numberinput {
    height: 1.5em;
    border-radius: 0.2em;
    box-shadow: 0 0 1px 1px rgba(255, 255, 255, 0.8) inset,
      0 1px 0 rgba(0, 0, 0, 0.3);
    text-align: center;
    border: 1px solid #777;
    background-color: #e6f5d4;
  }
</style>
