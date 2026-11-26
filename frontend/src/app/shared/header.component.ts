import { Component, HostListener, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="site-header" [class.scrolled]="scrolled()">
      <div class="container bar">
        <a routerLink="/" class="brand" (click)="menuOpen.set(false)">
          <span class="brand-mark" aria-hidden="true">S</span>
          <span class="brand-name">Staybook</span>
        </a>

        <nav class="nav-desktop" aria-label="Main">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Home</a>
          <a routerLink="/hotels" routerLinkActive="active">Hotels</a>
          <a routerLink="/manage-booking" routerLinkActive="active">Manage booking</a>
        </nav>

        <button class="menu-toggle" type="button" (click)="menuOpen.set(!menuOpen())"
                [attr.aria-expanded]="menuOpen()" aria-label="Toggle menu">
          <span [class.open]="menuOpen()"></span>
        </button>
      </div>

      <div class="nav-drawer" [class.open]="menuOpen()">
        <a routerLink="/" (click)="menuOpen.set(false)">Home</a>
        <a routerLink="/hotels" (click)="menuOpen.set(false)">Hotels</a>
        <a routerLink="/manage-booking" (click)="menuOpen.set(false)">Manage booking</a>
      </div>
    </header>
  `,
  styles: `
    .site-header {
      position: sticky; top: 0; z-index: 50;
      background: var(--paper);
      border-bottom: 1px solid transparent;
      transition: border-color .25s ease, background-color .25s ease;
    }
    .site-header.scrolled { border-bottom-color: var(--line); background: var(--surface); }
    .bar { display: flex; align-items: center; justify-content: space-between; height: 72px; }
    .brand { display: flex; align-items: center; gap: 12px; text-decoration: none; color: var(--ink); }
    .brand-mark {
      width: 38px; height: 38px; display: grid; place-items: center;
      background: var(--accent); color: var(--surface);
      font-family: var(--font-display); font-size: 22px; font-weight: 600;
      transition: transform .4s ease;
    }
    .brand:hover .brand-mark { transform: rotate(-90deg); }
    .brand-name { font-family: var(--font-display); font-size: 24px; font-weight: 600; letter-spacing: -.01em; }

    .nav-desktop { display: flex; gap: 36px; }
    .nav-desktop a {
      position: relative; color: var(--ink); text-decoration: none; font-weight: 600; font-size: 15px; padding: 6px 0;
    }
    .nav-desktop a::after {
      content: ''; position: absolute; left: 0; right: 0; bottom: 0; height: 2px;
      background: var(--accent); transform: scaleX(0); transform-origin: right; transition: transform .3s ease;
    }
    .nav-desktop a:hover::after, .nav-desktop a.active::after { transform: scaleX(1); transform-origin: left; }

    .menu-toggle { display: none; width: 44px; height: 44px; background: none; border: 0; cursor: pointer; position: relative; }
    .menu-toggle span, .menu-toggle span::before, .menu-toggle span::after {
      position: absolute; left: 10px; width: 24px; height: 2px; background: var(--ink); content: ''; transition: transform .3s ease, opacity .2s;
    }
    .menu-toggle span { top: 21px; }
    .menu-toggle span::before { left: 0; top: -7px; }
    .menu-toggle span::after { left: 0; top: 7px; }
    .menu-toggle span.open { background: transparent; }
    .menu-toggle span.open::before { transform: translateY(7px) rotate(45deg); }
    .menu-toggle span.open::after { transform: translateY(-7px) rotate(-45deg); }

    .nav-drawer {
      display: none; flex-direction: column; background: var(--surface);
      border-top: 1px solid var(--line); max-height: 0; overflow: hidden; transition: max-height .35s ease;
    }
    .nav-drawer.open { max-height: 260px; }
    .nav-drawer a { padding: 18px 24px; color: var(--ink); text-decoration: none; font-weight: 600; border-bottom: 1px solid var(--line); }

    @media (max-width: 760px) {
      .nav-desktop { display: none; }
      .menu-toggle { display: block; }
      .nav-drawer { display: flex; }
    }
  `,
})
export class HeaderComponent {
  readonly menuOpen = signal(false);
  readonly scrolled = signal(false);

  @HostListener('window:scroll')
  onScroll(): void {
    this.scrolled.set(window.scrollY > 8);
  }
}
