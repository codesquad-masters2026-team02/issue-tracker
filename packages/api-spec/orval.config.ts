import { defineConfig } from 'orval';

export default defineConfig({
  api: {
    input: './openapi.yaml',
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
