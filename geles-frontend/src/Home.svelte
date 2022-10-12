<script>
  import { onMount } from "svelte";
  import axios from "axios";

  import Catalogue from "./Catalogue.svelte";
  import { user } from "./stores";
  import {
    mapFlowersToWithFavorite,
    mapFlowerToWithFavorite
  } from "./util/flower";

  // Variable to hold fetched list
  let flowers: Flower[] = [];

  $: isLoggedIn = !!$user;

  async function getFlowers() {
    // Download data from server
    const flowersResponse = await axios.get<Omit<Flower, "favorite">[]>(
      "/flowers/",
      {
        withCredentials: true
      }
    );
    if (isLoggedIn) {
      const favoriteResponse = await axios.get<number[]>("/flowers/favorite", {
        withCredentials: true
      });
      flowers = mapFlowersToWithFavorite(
        flowersResponse.data,
        favoriteResponse.data
      );
    } else {
      flowers = mapFlowersToWithFavorite(flowersResponse.data, []);
    }
  }

  async function onFavoriteChange(flower: Omit<Flower, "favorite">) {
    const index = flowers.findIndex(_flower => _flower.id === flower.id);
    if (index === -1) {
      return;
    }

    const favoriteResponse = await axios.get<number[]>("/flowers/favorite", {
      withCredentials: true
    });
    flowers = [
      ...flowers.slice(0, index),
      mapFlowerToWithFavorite(flower, favoriteResponse.data),
      ...flowers.slice(index + 1)
    ];
  }

  // Run code on component mount (once)
  onMount(() => {
    getFlowers();
  });

  function onChange() {
    getFlowers();
  }
</script>

<Catalogue {flowers} {onChange} {onFavoriteChange} owner />
