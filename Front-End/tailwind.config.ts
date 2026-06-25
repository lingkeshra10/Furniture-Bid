import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#8B5E3C',
        secondary: '#C19A6B',
        accent: '#D97706',
        background: '#FAF7F2',
        card: '#FFFFFF',
        success: '#16A34A',
        text: '#1F2937',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      screens: {
        mobile: { max: '767px' },
        tablet: { min: '768px', max: '1024px' },
        desktop: { min: '1025px' },
      },
      minWidth: {
        touch: '44px',
      },
      minHeight: {
        touch: '44px',
      },
    },
  },
  plugins: [],
} satisfies Config;
