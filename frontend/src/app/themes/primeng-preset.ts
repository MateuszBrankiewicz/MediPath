import { definePreset } from '@primeuix/themes';
import Aura from '@primeuix/themes/aura';

export const MyPreset = definePreset(Aura, {
  primitive: {
    clinicblue: {
      50: '#f0f9ff',
      100: '#e0f2fe',
      200: '#bae6fd',
      300: '#7dd3fc',
      400: '#38bdf8',
      500: '#0ea5e9',
      600: '#0284c7',
      700: '#003249',
      800: '#075985',
      900: '#0c4a6e',
      950: '#082f49'
    },
    gray: {
      50: '#f9fafb',
      100: '#f3f4f6',
      200: '#e5e7eb',
      300: '#d1d5db',
      400: '#9ca3af',
      500: '#6b7280',
      600: '#4b5563',
      700: '#374151',
      800: '#1f2937',
      900: '#111827'
    }
  },
  semantic: {
    primary: {
      50: '{clinicblue.50}',
      100: '{clinicblue.100}',
      200: '{clinicblue.200}',
      300: '{clinicblue.300}',
      400: '{clinicblue.400}',
      500: '{clinicblue.500}',
      600: '{clinicblue.600}',
      700: '{clinicblue.700}',
      800: '{clinicblue.800}',
      900: '{clinicblue.900}',
      950: '{clinicblue.950}'
    },
    colorScheme: {
      light: {
        primary: {
          color: '{clinicblue.700}',
          inverseColor: '#ffffff',
          hoverColor: '{clinicblue.800}',
          activeColor: '{clinicblue.900}'
        },
        surface: {
          0: '#ffffff',
          50: '{gray.50}',
          100: '{gray.100}',
          200: '{gray.200}',
          300: '{gray.300}',
          400: '{gray.400}',
          500: '{gray.500}',
          600: '{gray.600}',
          700: '{gray.700}',
          800: '{gray.800}',
          900: '{gray.900}'
        },
        highlight: {
          background: '{clinicblue.50}',
          focusBackground: '{clinicblue.100}',
          color: '{clinicblue.900}',
          focusColor: '{clinicblue.950}'
        },
        formField: {
          hoverBorderColor: '{primary.color}'
        }
      },
      dark: {
        primary: {
          color: '{clinicblue.200}',
          inverseColor: '{clinicblue.950}',
          hoverColor: '{clinicblue.100}',
          activeColor: '{clinicblue.50}'
        },
        surface: {
          0: '#0f172a',
          50: '#1e293b',
          100: '#334155',
          200: '#475569',
          300: '#64748b',
          400: '#94a3b8',
          500: '#cbd5e1',
          600: '#e2e8f0',
          700: '#f1f5f9',
          800: '#f8fafc',
          900: '#ffffff'
        },
        highlight: {
          background: 'rgba(255, 255, 255, 0.08)',
          focusBackground: 'rgba(255, 255, 255, 0.16)',
          color: 'rgba(255,255,255,0.87)',
          focusColor: 'rgba(255,255,255,0.87)'
        },
        formField: {
          hoverBorderColor: '{primary.color}'
        }
      }
    },
    focusRing: {
      width: '2px',
      style: 'solid',
      color: '{primary.color}',
      offset: '2px'
    }
  }
});
