<script>
  import axios from "axios";
  import { onMount } from "svelte";
  import { navigate } from "svelte-routing";
  import Input from "./Input.svelte";
  import { isAxiosError } from "./util";
  import { getContext } from "svelte";
  import { notificationContextKey } from "./contexts";

  const { loading, success, error } = getContext<AppNotificationContext>(
    notificationContextKey
  );

  export let cartId: string;

  let cart: Cart = {
    id: 0,
    flowersInCart: []
  };

  let flowers: Flower[] = [];

  let errors: string[] = [];

  let order: OrderAdd = {
    address: "",
    contactPhone: "",
    cartId: +cartId
  };

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
        sum: flower.price * flowerInCart.amount
      };
    });
  }

  async function handleSubmit() {
    try {
      await loading(
        "Užsakoma...",
        axios.post("/orders/", order, { withCredentials: true })
      );
      success("Užsakyta");
      navigate("/");
    } catch (e) {
      if (isAxiosError(e)) {
        if (e.response) {
          if (e.response.status === 400) {
            errors = e.response.data.errors.map(
              error => `${error.field} ${error.defaultMessage}`
            );
            return;
          }
        }
      }
      error("Klaida pateikiant užsakymą");
    }
  }

  // Run code on component mount (once)
  onMount(async () => {
    await getFlowers();
    getCart();
  });
</script>

<div class="pagecontent">
  <h2>Užsakymas</h2>
  <table class="table">
    <tr>
      <th>Gėlė</th>
      <th>Kiekis</th>
      <th>Vnt. kaina</th>
      <th>Suma</th>
    </tr>
    {#each cart.flowersInCart as flowerInCart (flowerInCart.id)}
      <tr>
        <td>{flowerInCart.name}</td>
        <td align="right">{flowerInCart.amount}</td>
        <td align="right">{flowerInCart.price} €</td>
        <td align="right">{flowerInCart.sum?.toFixed(2)} €</td>
      </tr>
    {/each}
    <tr>
      <td class="total" align="right" colspan="3"><strong>Viso</strong></td>
      <td align="right">
        {cart.flowersInCart
          .reduce((prevValue, flowerInCart) => prevValue + flowerInCart.sum, 0)
          .toFixed(2)} €
      </td>
    </tr>
  </table>
  <form
    on:submit={e => {
      e.preventDefault();
      handleSubmit();
    }}
  >
    <Input
      label="Pristatymo adresas"
      bind:value={order.address}
      type="text"
      name="address"
    /><br />
    <Input
      label="Kontaktinis telefonas"
      bind:value={order.contactPhone}
      type="text"
      name="contactPhone"
    /><br />
    <button class="button save">Užsakyti</button>
    {#each errors as error}
      <p class="error">
        <i class="mdi mdi-alert-circle" />
        {error.slice(0, 1).toUpperCase()}{error.slice(1)}
      </p>
    {/each}
  </form>
</div>

<style>
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

  .total {
    text-align: center;
  }

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
    margin: 0;
  }
</style>
