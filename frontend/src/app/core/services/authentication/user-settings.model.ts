import {
  BackendLanguageCode,
  SupportedLanguage,
} from '../../services/translation/language.model';

export interface UserSettingsResponse {
  language: BackendLanguageCode;
  systemNotifications: boolean;
  userNotifications: boolean;
}

export interface UpdateUserSettingsRequest {
  language: BackendLanguageCode;
  systemNotifications: boolean;
  userNotifications: boolean;
}

export interface UserSettings {
  language: SupportedLanguage;
  systemNotifications: boolean;
  userNotifications: boolean;
}
