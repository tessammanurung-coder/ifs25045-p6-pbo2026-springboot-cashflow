import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated.ts';

const customConfig: UserConfigFn = (env) => ({
  // Here you can add custom Vite parameters
  // https://vitejs.dev/config/
  build: {
    // Menyediakan source map untuk JavaScript hasil build (audit Lighthouse "source maps").
    sourcemap: true,
  },
});

export default overrideVaadinConfig(customConfig);