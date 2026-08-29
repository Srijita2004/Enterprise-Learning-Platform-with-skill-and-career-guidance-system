import { Injectable, effect, signal } from '@angular/core';

export type AccentTheme =
  | 'blue'
  | 'violet'
  | 'emerald'
  | 'rose'
  | 'amber'
  | 'orange'
  | 'teal'
  | 'cyan'
  | 'magenta'
  | 'lime'
  | 'red';

const THEME_KEY = 'ssn_theme';
const ACCENT_KEY = 'ssn_accent';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly isDarkTheme = signal(this.readInitialTheme());
  readonly accentTheme = signal<AccentTheme>(this.readInitialAccent());

  constructor() {
    effect(() => {
      const dark = this.isDarkTheme();
      const accent = this.accentTheme();

      document.documentElement.classList.toggle(
        'light-theme',
        !dark
      );

      document.documentElement.setAttribute(
        'data-accent',
        accent
      );

      localStorage.setItem(
        THEME_KEY,
        dark ? 'dark' : 'light'
      );

      localStorage.setItem(
        ACCENT_KEY,
        accent
      );
    });
  }

  toggle(): void {
    this.isDarkTheme.update((dark) => !dark);
  }

  setLightMode(): void {
    this.isDarkTheme.set(false);
  }

  setDarkMode(): void {
    this.isDarkTheme.set(true);
  }

  setAccent(accent: AccentTheme): void {
    this.accentTheme.set(accent);
  }

  private readInitialTheme(): boolean {
    const stored = localStorage.getItem(THEME_KEY);

    if (stored === 'light') {
      return false;
    }

    if (stored === 'dark') {
      return true;
    }

    return !window.matchMedia?.(
      '(prefers-color-scheme: light)'
    ).matches;
  }

  private readInitialAccent(): AccentTheme {
    const stored =
      localStorage.getItem(ACCENT_KEY) as AccentTheme | null;

    const allowed: AccentTheme[] = [
      'blue',
      'violet',
      'emerald',
      'rose',
      'amber',
      'orange',
      'teal',
      'cyan',
      'magenta',
      'lime',
      'red'
    ];

    return stored && allowed.includes(stored)
      ? stored
      : 'blue';
  }
}