import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  imports: [RouterLink],
  template: `
    <footer class="site-footer">
      <div class="container grid">
        <div>
          <div class="brand">Staybook</div>
          <p class="muted">Handpicked stays across India. Book in under a minute, manage it any time with your reference.</p>
        </div>
        <div>
          <h4>Explore</h4>
          <a routerLink="/hotels">All hotels</a>
          <a [routerLink]="['/hotels']" [queryParams]="{ location: 'Goa' }">Beach stays</a>
          <a [routerLink]="['/hotels']" [queryParams]="{ location: 'Manali' }">Mountain stays</a>
        </div>
        <div>
          <h4>Your trip</h4>
          <a routerLink="/manage-booking">Find a booking</a>
          <a routerLink="/manage-booking">Cancel a booking</a>
        </div>
      </div>
      <div class="container legal">Demo project. Prices in Indian Rupees, taxes included.</div>
    </footer>
  `,
  styles: `
    .site-footer { background: var(--ink); color: var(--paper); padding: 64px 0 28px; margin-top: 96px; }
    .grid { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 48px; }
    .brand { font-family: var(--font-display); font-size: 28px; margin-bottom: 12px; }
    .muted { color: #B9C2BE; max-width: 360px; line-height: 1.6; }
    h4 { font-size: 13px; text-transform: uppercase; letter-spacing: .12em; color: var(--accent-soft); margin: 6px 0 16px; }
    a { display: block; color: var(--paper); text-decoration: none; margin-bottom: 10px; opacity: .85; transition: opacity .2s, transform .2s; }
    a:hover { opacity: 1; transform: translateX(4px); }
    .legal { border-top: 1px solid #33454B; margin-top: 48px; padding-top: 20px; font-size: 13px; color: #8E9A96; }
    @media (max-width: 760px) { .grid { grid-template-columns: 1fr; gap: 28px; } }
  `,
})
export class FooterComponent {}
