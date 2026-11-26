import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { addDays, todayIso, validateStay } from '../core/dates';

/** Location + dates search box. Navigates to /hotels with the search in the URL. */
@Component({
  selector: 'app-search-form',
  imports: [FormsModule],
  template: `
    <form class="search" [class.compact]="variant() === 'compact'" (ngSubmit)="submit()" novalidate>
      <label class="field where">
        <span>Where</span>
        <input type="text" name="location" [(ngModel)]="location" list="city-list"
               placeholder="City, area or hotel" autocomplete="off" maxlength="80" />
        <datalist id="city-list">
          @for (c of cities(); track c) { <option [value]="c"></option> }
        </datalist>
      </label>
      <label class="field">
        <span>Check-in</span>
        <input type="date" name="checkIn" [(ngModel)]="checkIn" [min]="today" (change)="onCheckInChange()" />
      </label>
      <label class="field">
        <span>Check-out</span>
        <input type="date" name="checkOut" [(ngModel)]="checkOut" [min]="checkIn || today" />
      </label>
      <button class="btn btn-primary go" type="submit">
        <span>Search</span>
        <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2.2" fill="none"/></svg>
      </button>
      @if (error()) { <p class="form-error" role="alert">{{ error() }}</p> }
    </form>
  `,
  styles: `
    .search {
      display: grid; grid-template-columns: 1.6fr 1fr 1fr auto; gap: 0;
      background: var(--surface); border: 1px solid var(--line);
      box-shadow: 0 18px 40px rgba(27, 42, 47, .12);
      position: relative;
    }
    .field { display: flex; flex-direction: column; padding: 14px 20px; border-right: 1px solid var(--line); min-width: 0; }
    .field span { font-size: 11px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; color: var(--muted); margin-bottom: 6px; }
    .field input { border: 0; padding: 4px 0; font-size: 16px; background: transparent; color: var(--ink); font-family: inherit; outline: none; width: 100%; }
    .field:focus-within { background: var(--paper); }
    .go { height: auto; padding: 0 32px; gap: 10px; }
    .go svg { transition: transform .25s ease; }
    .go:hover svg { transform: translateX(4px); }
    .form-error {
      position: absolute; left: 0; top: 100%; margin: 8px 0 0; padding: 8px 14px;
      background: var(--danger); color: #fff; font-size: 14px; animation: slideDown .25s ease;
    }
    .compact { box-shadow: none; }
    .compact .field { padding: 10px 16px; }
    @media (max-width: 860px) {
      .search { grid-template-columns: 1fr 1fr; }
      .where { grid-column: 1 / -1; border-right: 0; border-bottom: 1px solid var(--line); }
      .field:nth-child(3) { border-right: 0; }
      .go { grid-column: 1 / -1; padding: 16px; justify-content: center; }
      .form-error { position: static; grid-column: 1 / -1; margin: 0; }
    }
  `,
})
export class SearchFormComponent implements OnInit {
  readonly variant = input<'hero' | 'compact'>('hero');
  readonly initialLocation = input('');
  readonly initialCheckIn = input('');
  readonly initialCheckOut = input('');

  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  readonly today = todayIso();
  readonly cities = signal<string[]>([]);
  readonly error = signal<string | null>(null);

  location = '';
  checkIn = '';
  checkOut = '';

  ngOnInit(): void {
    this.location = this.initialLocation();
    this.checkIn = this.initialCheckIn() || addDays(this.today, 7);
    this.checkOut = this.initialCheckOut() || addDays(this.checkIn, 2);
    this.api.locations().subscribe({ next: (c) => this.cities.set(c), error: () => {} });
  }

  onCheckInChange(): void {
    if (this.checkIn && (!this.checkOut || this.checkOut <= this.checkIn)) {
      this.checkOut = addDays(this.checkIn, 1);
    }
  }

  submit(): void {
    const problem = validateStay(this.checkIn, this.checkOut);
    this.error.set(problem);
    if (problem) {
      return;
    }
    this.router.navigate(['/hotels'], {
      queryParams: {
        location: this.location.trim() || null,
        checkIn: this.checkIn,
        checkOut: this.checkOut,
      },
    });
  }
}
