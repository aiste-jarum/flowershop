<script>
  import { server_url } from "./index";
  import { Link } from "svelte-routing";
  import { beforeUpdate, getContext, onMount } from "svelte";
  import hash from "hash-sum";

  import axios from "axios";
  import noPhoto from "./assets/no-image.jfif";
  import { user } from "./stores";
  import { pluralize } from "./util";
  import { notificationContextKey } from "./contexts";

  const { loading, success, error } = getContext<AppNotificationContext>(
    notificationContextKey
  );

  // Variable to hold fetched list
  export let flowers: Flower[];
  export let owner: boolean | undefined = false;
  export let onChange: (() => void) | undefined = undefined;
  export let onFavoriteChange: ((flower: Flower) => void) | undefined =
    undefined;

  $: isLoggedIn = !!$user;
  $: isAdmin = $user && $user.admin;

  let cartId: number;
  let flowersWithAmount: (Flower & { amount: number })[] = [];
  let prevFlowerHash: string;

  async function handleDelete(id: number, name: string) {
    if (window.confirm(`Ar tikrai norite ištrinti gėlę ${name}?`)) {
      try {
        await loading(
          "Ištrinama...",
          axios.delete(`${server_url}/flowers/${id}`)
        );
        success("Gėlė ištrinta");
        if (onChange) onChange();
      } catch (e) {
        error("Klaida ištrinant gėlę");
      }
    }
  }

  async function handleFavoriteChange(flower: Flower) {
    const newFlower = (
      await axios.put(
        `/flowers/${flower.id}/favorite`,
        { favorite: !flower.favorite },
        { withCredentials: true }
      )
    ).data;
    if (onFavoriteChange) onFavoriteChange(newFlower);
  }

  async function handleToCart(flowerId: number, amount: number) {
    await axios.post("/carts/flowers/", {
      amount,
      flowerId,
      cartId
    });
    success(
      `${pluralize(
        "Pridėta",
        "Pridėtos",
        "Pridėta",
        amount
      )} ${amount} ${pluralize("gėlė", "gėlės", "gėlių", amount)}`
    );
  }

  onMount(() => {
    if ($user) {
      cartId = $user.cartId;
    }
  });

  beforeUpdate(() => {
    if (flowers) {
      if (prevFlowerHash !== hash(flowers)) {
        flowersWithAmount = flowers.map(flower => ({ ...flower, amount: 1 }));
      } else {
        // Check for amounts that have been set below 1
        // Filter flowers that have amount less than one
        const invalidAmounts = flowersWithAmount.filter(
          flower => flower.amount < 1
        );

        // Update invalid flower amounts to 1
        flowersWithAmount = flowersWithAmount.map(flower => ({
          ...flower,
          amount:
            // If flower belongs to invalidAmounts
            invalidAmounts.some(
              flowerWithAmount => flowerWithAmount.id === flower.id
            )
              ? 1
              : flower.amount
        }));
      }
    }
    prevFlowerHash = hash(flowers);
  });
</script>

<div class="flower-list">
  <!--
    Map each `flowers` entry as an HTML element `li` 
    When `flowers` updates, so does this block
  -->
  {#each flowersWithAmount as flower (flower.id)}
    <div class="flower-list-item">
      <div class="flower-list-item-photo-container">
        <Link to="/flower/{flower.id}" class="link-wrapper">
          {#if flower.photo}
            <img
              class="flower-list-item-photo"
              src="{server_url}/files/{flower.photo}"
              alt={flower.name}
            />
          {:else}
            <img
              class="flower-list-item-photo"
              src={noPhoto}
              alt="Nėra nuotraukos"
            />
          {/if}
        </Link>
      </div>
      {#if isLoggedIn && !isAdmin}
        <div
          class="flower-list-item-favorite"
          on:click={() => handleFavoriteChange(flower)}
        >
          {#if flower.favorite}
            <i class="mdi mdi-heart" />
          {:else}
            <i class="mdi mdi-heart-outline" />
          {/if}
        </div>
      {/if}
      <div class="flower-list-item-name">{flower.name}</div>
      <div class="flower-list-item-price">{flower.price} €</div>
      <p class="flower-list-item-description">
        {flower.description}
      </p>
      {#if isAdmin}
        <Link to="/update/{flower.id}" class="button edit">Redaguoti</Link>
        <button
          class="button delete"
          on:click={() => handleDelete(flower.id, flower.name)}
        >
          Ištrinti
        </button>
      {/if}
      {#if isLoggedIn && !isAdmin}
        <input
          class="numberinput"
          type="number"
          min="1"
          max="100"
          bind:value={flower.amount}
          size="7"
        />
        <button
          class="button add"
          on:click={() => handleToCart(flower.id, flower.amount)}
        >
          Į krepšelį
        </button>
      {/if}
    </div>
  {/each}
</div>

<style>
  .flower-list {
    display: flex;
    flex-wrap: wrap;
  }

  .flower-list-item {
    box-shadow: black 0 0 4px;
    margin: 16px;
    padding: 8px;
    min-width: 200px;
    max-width: 300px;
    flex-basis: calc(25% - 48px);
    position: relative;
    background-color: rgb(245, 253, 223);
    border-radius: 4px;
  }

  .flower-list-item-name {
    font-size: 24px;
  }

  .flower-list-item-photo {
    width: 100%;
  }

  .flower-list-item-description {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 4;
    -webkit-box-orient: vertical;
    min-height: 76px;
    /* text-align: justify; */
  }

  .flower-list-item-favorite {
    background-color: rgb(245, 253, 223);
    border: 1px solid black;
    width: 48px;
    height: 48px;
    position: absolute;
    top: 8px;
    right: 8px;
    cursor: pointer;
  }

  .mdi {
    font-size: 48px;
    line-height: 1;
  }
  .mdi.mdi-heart {
    color: red;
  }

  @media (max-width: 1024px) {
    .flower-list-item {
      flex-basis: calc(33.3333333% - 48px);
    }
  }

  @media (max-width: 776px) {
    .flower-list-item {
      flex-basis: calc(50% - 48px);
    }
  }

  @media (max-width: 528px) {
    .flower-list-item {
      flex-basis: calc(100% - 48px);
    }
  }
</style>
