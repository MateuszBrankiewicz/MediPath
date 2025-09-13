import { DialogDesignTokens } from '@primeuix/themes/types/dialog';

export const dialogDesign: Partial<DialogDesignTokens> = {
  css: () => `

    .p-dialog-header {
      background: var(--p-primary-color);
      color: white;
      border-bottom: none;
      border-radius: 10px
    }

    .p-dialog{
      background: var(--p-surface-100)
    }

    .p-dialog-title {
      font-size: 1.5rem;
      font-weight: 300;
      margin: 0;
    }

    .p-dialog-header-icon {
      color: white;
      width: 2rem;
      height: 2rem;
    }

    .p-dialog-header-icon:hover {
      background-color: rgba(255, 255, 255, 0.1);
      color: white;
    }




  `,
};
