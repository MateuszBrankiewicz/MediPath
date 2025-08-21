import { MenuDesignTokens } from '@primeuix/themes/types/menu';

export const menuComponent: MenuDesignTokens = {
  root: {
    background: '{primary.color}',
    color: '{surface.400}',
    borderColor: 'none',
  },
  list: {
    gap: '1rem',
  },
  item: {
    focusBackground: 'rgba(255,255,255,0.4)',
    color: '{surface.400}',

    focusColor: '{surface.200}',
    icon: {
      color: '{surface.400}',
    },
  },
};
