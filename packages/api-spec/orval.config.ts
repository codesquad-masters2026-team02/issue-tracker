import { defineConfig } from 'orval';

export default defineConfig({
  api: {
    input: 'http://localhost:8080/v3/api-docs',
    output: {
      target: './src/generated/index.ts',
      client: 'react-query',
      mode: 'split',
      override: {
        mutator: {
          path: './src/mutator/axios.ts',
          name: 'customInstance',
        },
      },
    },
  },
});
