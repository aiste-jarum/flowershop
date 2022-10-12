<script>
  import { onMount } from "svelte";
  import Catalogue from "./Catalogue.svelte";
  import axios from "axios";
  import { mapFlowerToWithFavorite } from "./util/flower";

  // Variable to hold fetched list
  let flowers: Flower[] = [];

  async function getFlowers() {
    // Download data from server
    const response = await axios.get("/flowers/?favorite=true", {
      withCredentials: true
    });
    flowers = response.data.map(flower => ({ ...flower, favorite: true }));
  }

  async function onFavoriteChange(flower: Flower) {
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
</script>

<Catalogue {flowers} {onFavoriteChange} />
