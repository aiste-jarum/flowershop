<script>
  import axios from "axios";
  import { onMount } from "svelte";

  import Catalogue from "./Catalogue.svelte";
  import SearchBar from "./SearchBar.svelte";
  import { mapFlowersToWithFavorite } from "./util/flower";

  interface Filter {
    sort: string;
    sortType: string;
    filters: {
      name: string;
      value: string | number;
    }[];
  }

  let filter: Filter = {
    sort: "price",
    sortType: "asc",
    filters: []
  };

  // Variable to hold input string
  let query = "";
  // Variable to hold fetched list
  let flowers: Flower[] = [];
  let favoriteFlowers: number[];

  // Dependencies of this block are calculated by Svelte
  // Every time `query` changes, this block of code runs
  $: {
    let minPrice = filter.filters.findIndex(
      filter => filter.name === "minPrice"
    );
    let maxPrice = filter.filters.findIndex(
      filter => filter.name === "maxPrice"
    );

    if (minPrice != -1 && maxPrice != -1) {
      if (+filter.filters[minPrice].value <= +filter.filters[maxPrice].value) {
        axios
          .post<Omit<Flower, "favorite">[]>(
            `/flowers/filter/?q=${query}`,
            filter
          )
          .then(response => {
            flowers = mapFlowersToWithFavorite(response.data, favoriteFlowers);
          });
      }
    } else {
      axios.post(`/flowers/filter/?q=${query}`, filter).then(response => {
        flowers = mapFlowersToWithFavorite(response.data, favoriteFlowers);
      });
    }
  }

  interface SortableField {
    field: string;
    label: string;
  }

  const sortableFields: SortableField[] = [
    { field: "price", label: "Kaina" },
    { field: "name", label: "Pavadinimas" }
  ];

  function handleFilter(name: string, value: string | number) {
    const index = filter.filters.findIndex(filter => filter.name === name);
    if (index > -1) {
      if (!value) {
        filter.filters = [
          ...filter.filters.slice(0, index),
          ...filter.filters.slice(index + 1)
        ];
      } else {
        filter.filters[index].value = value;
      }
    } else {
      filter.filters = [...filter.filters, { name, value }];
    }
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
      { ...flower, favorite: favoriteResponse.data.includes(flower.id) },
      ...flowers.slice(index + 1)
    ];
  }

  onMount(() => {
    axios
      .get<number[]>("/flowers/favorite", {
        withCredentials: true
      })
      .then(response => {
        favoriteFlowers = response.data;
      })
      .catch(() => {
        favoriteFlowers = [];
      });
  });
</script>

<SearchBar bind:query />

<div class="sort">
  <div>
    <h3>Rikiavimas</h3>
    <span class="sort-inputs">
      <select class="selectinput" bind:value={filter.sort}>
        {#each sortableFields as field}
          <option value={field.field}>{field.label}</option>
        {/each}
      </select>
      <select class="selectinput" bind:value={filter.sortType}>
        <option value="asc">Didėjimo tvarka</option>
        <option value="desc">Mažėjimo tvarka</option>
      </select><br /><br />
    </span>
  </div>
  <div>
    <h3>Filtravimas</h3>
    <label class="priceLabel" for="priceInput">Kaina</label>
    <span class="filter-price-inputs">
      <input
        id="priceInput"
        on:input={e => handleFilter("minPrice", e.currentTarget.value)}
        class="priceInput numberinput"
        type="number"
        min={0}
        placeholder="Nuo"
        step={0.1}
      />
      <input
        on:input={e => handleFilter("maxPrice", e.currentTarget.value)}
        class="priceInput numberinput"
        type="number"
        min={0}
        placeholder="Iki"
        step={0.1}
      />
    </span>
  </div>
</div>

<div style="padding:6px">
  <Catalogue {flowers} {onFavoriteChange} />
</div>

<!-- <div class="filter">
  <input type="range" id="volume" name="volume" min="0" max="11" />
</div> -->
<style>
  .priceLabel {
    display: inline-block;
    margin-bottom: 8px;
    margin-right: 8px;
  }

  .priceInput {
    width: 60px;
  }

  .sort {
    margin-left: 20px;
    display: flex;
    align-items: flex-start;
  }

  .sort > div:not(:first-child) {
    margin-left: 20px;
  }

  .sort-inputs {
    display: flex;
    flex-wrap: wrap;
  }

  .selectinput {
    margin-left: 0;
    margin-right: 4px;
    margin-bottom: 4px;
    height: 2.3em;
  }

  .filter-price-inputs {
    display: inline-flex;
    flex-wrap: wrap;
  }

  .filter-price-inputs .numberinput {
    margin-left: 0;
    margin-right: 4px;
    margin-bottom: 4px;
  }
</style>
