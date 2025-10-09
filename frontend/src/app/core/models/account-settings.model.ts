import { UserSettings } from '../services/authentication/user-settings.model';
import {
  DEFAULT_LANGUAGE,
  SupportedLanguage,
} from '../services/translation/language.model';

export interface LanguageOption {
  code: SupportedLanguage;
  name: string;
}

export type AccountSettingsForm = UserSettings;

export const DEFAULT_FORM: AccountSettingsForm = {
  language: DEFAULT_LANGUAGE,
  systemNotifications: true,
  userNotifications: false,
};
