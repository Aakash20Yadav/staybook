import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  template: `
    <section class="container nf fade-up">
      <span class="code">404</span>
      <h1>This page checked out early</h1>
      <p>The page you are looking for doesn't exist or has moved.</p>
      <a routerLink="/" class="btn btn-primary">Back to home</a>
    </section>
  `,
  styles: `
    .nf { text-align: center; padding: 120px 24px 40px; }
    .code { font-family: var(--font-display); font-size: 120px; color: var(--accent); line-height: 1; }
    h1 { font-size: 40px; margin: 12px 0; }
    p { color: var(--muted); margin-bottom: 28px; }
  `,
})
export class NotFoundComponent {}
