<script>
  import { onMount } from "svelte";
  import { user } from "./stores";
  import { server_url } from "./index";
  import { OrderStatus } from "./enums";
  import axios from "axios";
  import { getContext } from "svelte";
  import { notificationContextKey } from "./contexts";
  import { AppNotificationType } from "./enums";

  const { addNotification } = getContext<AppNotificationContext>(
    notificationContextKey
  );

  export let id: string;
  $: isAdmin = $user && $user.admin;
  $: if (isAdmin && order != null) loadUser();

  interface OrderEdit {
    orderFlowers: OrderFlower[];
    address: string;
    contactPhone: string;
    version: number;
  }

  enum OrderEditButton {
    SAVE,
    PAY,
    CANCEL,
    ADMIN_CONFIRMPAY,
    ADMIN_CONFIRMDELIVERED
  }

  let order: Order;
  let editDto: OrderEdit = {
    orderFlowers: [],
    contactPhone: "",
    address: "",
    version: 0
  };

  let editedUserName: string = "";
  let flowers: Flower[];
  let orderTotal: number = 0;

  function flowerFromId(id: number) {
    const f = flowers.find(f => f.id == id);
    if (f == null) throw new Error("Flower not found");
    return f;
  }

  function recalcSum() {
    orderTotal = order.orderFlowers.map(rowSum).reduce((a, b) => a + b, 0);
  }

  async function loadUser() {
    let userRes = await axios.get(`/users/${order.userId}`, {
      withCredentials: true
    });
    editedUserName = userRes.data.username;
  }

  function rowSum(flower: OrderFlower) {
    return flower.quantity * flowerFromId(flower.flowerId).price;
  }

  function disableButton(status: OrderStatus, button: OrderEditButton) {
    let enableButton: boolean = false;
    switch (button) {
      case OrderEditButton.SAVE:
        if (isAdmin) {
          enableButton =
            status != OrderStatus.CANCELED && status != OrderStatus.DELIVERED;
        } else {
          enableButton = status == OrderStatus.UNPAID;
        }
        break;
      case OrderEditButton.CANCEL:
        enableButton =
          status != OrderStatus.CANCELED && status != OrderStatus.DELIVERED;
        break;
      case OrderEditButton.PAY:
        enableButton = status == OrderStatus.UNPAID;
        break;
      case OrderEditButton.ADMIN_CONFIRMPAY:
        enableButton = status == OrderStatus.PAID;
        break;
      case OrderEditButton.ADMIN_CONFIRMDELIVERED:
        enableButton = status == OrderStatus.CONFIRMED;
        break;
    }
    return !enableButton;
  }

  function EnableOrderEdits(): boolean {
    let enable: boolean = false;
    if (isAdmin) {
      enable =
        order.orderStatus != OrderStatus.CANCELED &&
        order.orderStatus != OrderStatus.DELIVERED;
    } else {
      enable = order.orderStatus == OrderStatus.UNPAID;
    }
    return enable;
  }

  function handleDelete(fl: OrderFlower) {
    let index = editDto.orderFlowers.indexOf(fl);
    if (index !== -1) {
      editDto.orderFlowers = [
        ...editDto.orderFlowers.slice(0, index),
        ...editDto.orderFlowers.slice(index + 1)
      ];
    }
    recalcSum();
  }

  function handleError(reason: any) {
    let errString: string = "";
    if (reason.response) {
      errString = "Error: HTTP Status " + reason.response.status;
    } else if (reason.request) {
      errString = reason.request;
    } else {
      // Something happened in setting up the request that triggered an Error
      errString = reason;
    }
    console.error(reason);
    addNotification(errString, AppNotificationType.DANGER);
  }

  async function getOrderData() {
    let resp = await axios.get(`/orders/${id}`, { withCredentials: true });
    order = resp.data;
    editDto.orderFlowers = order.orderFlowers;
    editDto.address = order.address;
    editDto.contactPhone = order.contactPhone;
    editDto.version = order.version;
    recalcSum();
  }

  async function handleUpdate() {
    try {
      await axios.put(`/orders/${id}/edit`, editDto);
      await getOrderData();
      addNotification("Užsakymas atnaujintas", AppNotificationType.SUCCESS);
    } catch (err) {
      handleError(err);
    }
  }

  async function handlePay() {
    try {
      await axios.post(`/orders/${id}/pay`, { version: order.version });
      await getOrderData();
      addNotification("Užsakymas apmokėtas", AppNotificationType.SUCCESS);
    } catch (err) {
      handleError(err);
    }
  }

  async function handleCancel() {
    try {
      await axios.post(`/orders/${id}/cancel`, { version: order.version });
      await getOrderData();
      addNotification("Užsakymas atšauktas", AppNotificationType.SUCCESS);
    } catch (err) {
      handleError(err);
    }
  }
  async function handleConfirmOrder() {
    try {
      await axios.post(`/orders/${id}/confirm`, { version: order.version });
      await getOrderData();
      addNotification("Užsakymas patvirtintas", AppNotificationType.SUCCESS);
    } catch (err) {
      handleError(err);
    }
  }
  async function handleConfirmDelivered() {
    try {
      await axios.post(`/orders/${id}/confirmDelivery`, {
        version: order.version
      });
      await getOrderData();
      addNotification(
        "Užsakymo pristatymas patvirtintas",
        AppNotificationType.SUCCESS
      );
    } catch (err) {
      handleError(err);
    }
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

  // Run code on component mount (once)
  onMount(async () => {
    const response = await axios.get(`/flowers/`);
    flowers = response.data;
    await getOrderData();
  });
</script>

{#if order == null}
  <h2>Kraunama....</h2>
{:else}
  <h2>Užsakymas</h2>
  <div class="row">
    <div class="column">
      <div class="flowers-table">
        <table>
          <tr>
            <th colspan="2">Gėlė</th>
            <th>Kiekis</th>
            <th>Vnt. kaina</th>
            <th>Suma</th>
            {#if order.orderStatus == "UNPAID"}
              <th>Veiksmai</th>
            {/if}
          </tr>
          {#each editDto.orderFlowers as flower (flower.id)}
            <tr>
              <div class="imagecontainer">
                {#if flowerFromId(flower.flowerId).photo != null}
                  <img
                    class="flower-list-item-photo"
                    src={`${server_url}/files/${
                      flowerFromId(flower.flowerId).photo
                    }`}
                    alt={flowerFromId(flower.flowerId).name}
                    width="80"
                    height="80"
                  />
                {/if}
              </div>
              <td>{flowerFromId(flower.flowerId).name}</td>
              <td>
                {#if order.orderStatus == "UNPAID"}
                  <input
                    type="number"
                    bind:value={flower.quantity}
                    min="1"
                    max="100"
                    size="5"
                    on:input={recalcSum}
                  />
                {:else}
                  {flower.quantity}
                {/if}
              </td>
              <td class="number">{flowerFromId(flower.flowerId).price} €</td>
              <td class="number">{rowSum(flower).toFixed(2)} €</td>
              {#if order.orderStatus == "UNPAID"}
                <td>
                  <button
                    class="button delete"
                    on:click={() => handleDelete(flower)}>Pašalinti</button
                  >
                </td>
              {/if}
            </tr>
          {/each}
        </table>
        <div class="row buttonsrow">
          <button
            class="button"
            disabled={disableButton(order.orderStatus, OrderEditButton.SAVE)}
            on:click={handleUpdate}
          >
            Išsaugoti pakeitimus
          </button>
          <button
            class="button"
            disabled={disableButton(order.orderStatus, OrderEditButton.PAY)}
            on:click={handlePay}
          >
            Apmokėti
          </button>

          <button
            class="button"
            disabled={disableButton(order.orderStatus, OrderEditButton.CANCEL)}
            on:click={handleCancel}
          >
            Atšaukti
          </button>
        </div>
      </div>
    </div>
    <div class="column">
      <div class="editorder-inputs">
        {#if isAdmin}
          <div class="editorder-inputrow" style="height: 30px;">
            <label for="user">Vartotojas</label>
            <div id="user" class="editorder-textoutput">
              {editedUserName}
            </div>
          </div>
        {/if}
        <div class="editorder-inputrow">
          <label for="orderId">Užsakymo ID</label>
          <div id="orderId" class="editorder-textoutput">
            <strong>{order.id}</strong>
          </div>
        </div>
        <div class="editorder-inputrow">
          <label for="orderStatus">Užsakymo Būsena</label>
          <div id="orderStatus" class="editorder-textoutput">
            {orderStatusString(order.orderStatus)}
          </div>
        </div>
        <div class="editorder-inputrow">
          <label for="adress">Adresas</label>
          {#if EnableOrderEdits()}
            <input
              class="editorder-textinput"
              id="address"
              type="text"
              bind:value={editDto.address}
            />
          {:else}
            <div id="address" class="editorder-textoutput">
              {order.address}
            </div>
          {/if}
        </div>
        <div class="editorder-inputrow">
          <label for="phone">Telefonas</label>
          {#if EnableOrderEdits()}
            <input
              class="editorder-textinput"
              type="text"
              id="phone"
              bind:value={editDto.contactPhone}
            />
          {:else}
            <div id="phone" class="editorder-textoutput">
              {order.contactPhone}
            </div>
          {/if}
        </div>
        <div class="editorder-inputrow">
          <label for="totalsum"> Bendra užsakymo suma</label>
          <div id="totalsum" class="editorder-textoutput">
            {orderTotal.toFixed(2)} &euro;
          </div>
        </div>
        <div class="editorder-inputrow">
          <label for="orderDate">Užsakymo data</label>
          <div id="orderDate" class="editorder-textoutput">
            {formatDate(order.createdDate)}
          </div>
        </div>

        {#if isAdmin}
          <div class="editorder-inputrow">
            <div style="padding-top:40px;padding-bottom:40px;">
              <strong>Administratoriaus funkcijos</strong>
            </div>
          </div>
          <div class="editorder-inputrow">
            <button
              class="button"
              disabled={disableButton(
                order.orderStatus,
                OrderEditButton.ADMIN_CONFIRMPAY
              )}
              on:click={handleConfirmOrder}
            >
              Patvirtinti apmokėjimą
            </button>
          </div>
          <div class="editorder-inputrow">
            <button
              class="button"
              disabled={disableButton(
                order.orderStatus,
                OrderEditButton.ADMIN_CONFIRMDELIVERED
              )}
              on:click={handleConfirmDelivered}
            >
              Patvirtinti užsakymo užbaigimą (pristatymą/atsiėmimą)
            </button>
          </div>
        {/if}
      </div>
    </div>
  </div>
{/if}

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
    width: 100%;
    background-color: #d9d9d9;
    border: 4px solid #8ebf42;
    margin-bottom: 10px;
  }

  th,
  td {
    padding: 4px 8px;
  }
  th {
    margin: auto;
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

  img {
    margin: 8px;
    /* Weird bug with table cell height */
    margin-bottom: 4px;
  }

  .row {
    display: flex;
  }

  .column {
    flex-basis: 50%;
  }
  .flowers-table {
    width: fit-content;
  }

  .editorder-inputs {
    display: table;
  }
  .editorder-inputrow {
    display: table-row;
  }
  .editorder-textinput {
    display: table-cell;
    margin-left: 30px;
  }

  .editorder-textoutput {
    display: table-cell;
    padding-left: 30px;
  }

  label {
    display: table-cell;
  }

  .button {
    box-sizing: content-box;
  }
</style>
