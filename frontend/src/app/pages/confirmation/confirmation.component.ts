import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Booking } from '../../core/models';
import { ImgFallbackDirective } from '../../shared/img-fallback.directive';
import { LAST_BOOKING_KEY } from '../../core/storage';

@Component({
  selector: 'app-confirmation',
  imports: [RouterLink, CurrencyPipe, DatePipe, ImgFallbackDirective],
  templateUrl: './confirmation.component.html',
  styleUrl: './confirmation.component.scss',
})
export class ConfirmationComponent implements OnInit {
  readonly reference = input.required<string>();
  readonly booking = signal<Booking | null>(null);
  readonly copied = signal(false);

  ngOnInit(): void {
    // Prefer the booking handed over by navigation; fall back to sessionStorage after a refresh.
    const fromState = (history.state as { booking?: Booking } | null)?.booking;
    let found: Booking | null = fromState ?? null;
    if (!found) {
      try {
        const stored = sessionStorage.getItem(LAST_BOOKING_KEY);
        found = stored ? (JSON.parse(stored) as Booking) : null;
      } catch {
        found = null;
      }
    }
    if (found && found.reference === this.reference()) {
      this.booking.set(found);
    }
  }

  async copy(): Promise<void> {
    try {
      await navigator.clipboard.writeText(this.reference());
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    } catch {
      this.copied.set(false);
    }
  }
}
