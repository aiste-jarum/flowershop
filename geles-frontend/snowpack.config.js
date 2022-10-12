/** @type {import("snowpack").SnowpackUserConfig } */
module.exports = {
  mount: {
    public: { url: "/", static: true },
    src: { url: "/dist" }
  },
  plugins: [
    [
      "@snowpack/plugin-svelte",
      {
        hmrOptions: {
          preserveLocalState: true
        }
      }
    ],
    "@snowpack/plugin-dotenv",
    [
      "@snowpack/plugin-typescript",
      {
        /* Yarn PnP workaround: see https://www.npmjs.com/package/@snowpack/plugin-typescript */
        ...(process.versions.pnp ? { tsc: "yarn pnpify tsc" } : {})
      }
    ]
  ],
  routes: [
    /* Enable an SPA Fallback in development: */
    { match: "all", src: ".*/index.css", dest: "/index.css" },
    { match: "routes", src: ".*", dest: "/index.html" }
  ],
  optimize: {
    /* Example: Bundle your final build: */
    // "bundle": true,
  },
  packageOptions: {
    /* ... */
  },
  devOptions: {
    port: 3000
  },
  buildOptions: {
    /* ... */
  }
};
