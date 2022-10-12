<script>
  import { onMount } from "svelte";
  import { user } from "./stores";
  import axios from "axios";
  import {OrderStatus} from "./enums"
  import { Link } from "svelte-routing";

  $: isAdmin = $user && $user.admin;
  $: if (isAdmin) loadUsers();

  enum SortBy {
    ID,
    DATE,
    STATUS,
    USER
  }

  const orderStatusSortingOrder = {
    [OrderStatus.UNPAID]: 1,
    [OrderStatus.PAID]: 2,
    [OrderStatus.CONFIRMED]: 3,
    [OrderStatus.DELIVERED]: 4,
    [OrderStatus.CANCELED]: 5
  } as const;

  let users: User[] = [];
  let orders: Order[] = [];

  function userFromId(id: number): User {
    return users.find(f => f.id == id) as User;
  }
  
  function formatDate(date: string) {
    var d = new Date(date),
      month = "" + (d.getMonth() + 1),
      day = "" + d.getDate(),
      year = d.getFullYear();

    if (month.length < 2) month = "0" + month;
    if (day.length < 2) day = "0" + day;

    return [year, month, day].join("-");
  }

  function orderStatusString(status: OrderStatus) {
    switch (status) {
      case OrderStatus.UNPAID:
        return "Neapmokėta";
      case OrderStatus.PAID:
        return "Apmokėtas";
      case OrderStatus.CONFIRMED:
        return "Patvirtintas";
      case OrderStatus.DELIVERED:
        return "Pristatytas";
      case OrderStatus.CANCELED:
        return "Atšauktas";
      default:
        return "???????";
    }
  }

  function sort(sortingOrder: SortBy) {
    switch (sortingOrder) {
      case SortBy.DATE:
        orders = orders.sort(
          (a: Order, b: Order) =>
            +new Date(a.createdDate) - +new Date(b.createdDate)
        );
        break;
      case SortBy.ID:
        orders = orders.sort((a: Order, b: Order) => a.id - b.id);
        break;
      case SortBy.STATUS:
        orders = orders.sort(
          (a, b) =>
            orderStatusSortingOrder[a.orderStatus] -
            orderStatusSortingOrder[b.orderStatus]
        );
        break;
      case SortBy.USER:
        orders = orders.sort((a: Order, b:Order) =>(userFromId(a.userId).username > userFromId(b.userId).username ? -1 : 1));
        break;
      default:
        throw "Unknown sorting order :(";
    }
  }

  async function getOrders() {
    const response = await axios.get<Order[]>(`/orders/`, {
      withCredentials: true
    });

    orders = response.data;
    sort(SortBy.ID);
  }
  
  async function loadUsers() {
    let usersRes = await axios.get(`/users/all/`, { withCredentials: true });
    users = usersRes.data;
  }

  // Run code on component mount (once)
  onMount(async () => {
    await getOrders();
  });
</script>

<div class="order-list">
  {#if isAdmin}
    <h2>Visi užsakymai</h2>
  {:else}
    <h2>Mano užsakymai</h2>
  {/if}
  {#if orders.length > 0}
    <table class="orders-table">
      <tr>
        <th on:click={() => sort(SortBy.ID)}>
          <div style="cursor:pointer;">Užsakymo ID</div>
        </th>
        <th on:click={() => sort(SortBy.DATE)}>
          <div style="cursor:pointer;">Užsakymo data</div>
        </th>
        {#if isAdmin}
          <th on:click={() => sort(SortBy.USER)}>
            <div style="cursor:pointer;">Vartotojas</div>
          </th>
        {/if}
        <th>Kiekis</th>
        <th>Suma</th>
        <th on:click={() => sort(SortBy.STATUS)}
          ><div style="cursor:pointer;">Statusas</div></th
        >
        <th>Veiksmai</th>
      </tr>
      {#each orders as order, i (order.id)}
        <tr
          class="order-row {order.orderStatus === OrderStatus.UNPAID
            ? 'order-row-unpaid'
            : ''} {i % 2 === 0 ? 'order-row-grey' : ''}"
        >
          <td>
            {order.id}
          </td>
          <td>
            {formatDate(order.createdDate)}
          </td>
          {#if isAdmin}
            <td>
              {userFromId(order.userId).username}
            </td>
          {/if}
          <td>
            {order.orderFlowers.reduce(
              (acc, flower) => acc + flower.quantity,
              0
            )}
          </td>
          <td>
            {order.totalOrderPrice.toFixed(2)} €
          </td>
          <td>
            <span
              class={order.orderStatus === OrderStatus.UNPAID
                ? "order-cell-unpaid"
                : ""}
            >
              {orderStatusString(order.orderStatus)}
            </span>
          </td>
          <td>
            <Link to={`/order/edit/${order.id}`}>Daugiau info</Link>
          </td>
        </tr>
      {/each}
    </table>
  {:else}
    <p>Nėra užsakymų!</p>
  {/if}
</div>

<style>
  .order-list {
    margin-left: 40px;
    margin-right: 40px;
  }

  .orders-table {
    width: 100%;
  }

  /* .order-row {
    opacity: 1;
  }

  .order-row-grey {
    opacity: 0.75;
  } */

  .order-row-unpaid {
    background-color: red;
  }

  /* .order-row:hover {
    opacity: 0.75;
  } */

  .order-cell-unpaid {
    font-weight: bold;
  }

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
    background-color: #d9d9d9;
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
    background-color: white;
  }
</style>
