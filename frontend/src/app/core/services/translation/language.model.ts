export type SupportedLanguage = 'en' | 'pl';

export type BackendLanguageCode = 'EN' | 'PL';

export const DEFAULT_LANGUAGE: SupportedLanguage = 'en';

export function toBackendLanguage(
  language: SupportedLanguage,
): BackendLanguageCode {
  return language.toUpperCase() as BackendLanguageCode;
}

export function fromBackendLanguage(
  language: BackendLanguageCode | string | null | undefined,
): SupportedLanguage {
  if (!language) {
    return DEFAULT_LANGUAGE;
  }

  const normalized = language.toLowerCase();
  return normalized === 'en' ? 'en' : 'pl';
}
