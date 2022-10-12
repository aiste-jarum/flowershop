<script>
  // Variable to hold input string
  export let query: string;
</script>

<div class="search-bar">
  <span>🔍</span>
  <input bind:value={query} class="search-input" autocomplete="off" />
</div>

<style>
  .search-bar {
    box-shadow: black 0 0 4px 0;
    border-radius: 4px;
    display: flex;
    padding: 8px 12px;
    width: 300px;
    margin-left: 20px;
    margin-top: 20px;
  }

  .search-input {
    border: none;
    font-size: 1rem;
    flex-grow: 1;
    margin-left: 8px;
    outline: none;
    background-color: ivory;
  }
</style>
