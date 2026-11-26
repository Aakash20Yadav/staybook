import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { errorMessage } from '../../core/errors';
import { Booking } from '../../core/models';
import { ImgFallbackDirective } from '../../shared/img-fallback.directive';

@Component({
  selector: 'app-manage-booking',
  imports: [FormsModule, CurrencyPipe, DatePipe, RouterLink, ImgFallbackDirective],
  templateUrl: './manage-booking.component.html',
  styleUrl: './manage-booking.component.scss',
})
export class ManageBookingComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  reference = '';
  email = '';
  readonly formErrors = signal<Record<string, string>>({});

  readonly booking = signal<Booking | null>(null);
  readonly searching = signal(false);
  readonly error = signal<string | null>(null);

  readonly confirmOpen = signal(false);
  readonly cancelling = signal(false);
  readonly cancelError = signal<string | null>(null);
  readonly justCancelled = signal(false);

  ngOnInit(): void {
    const qp = this.route.snapshot.queryParamMap;
    this.reference = qp.get('reference') ?? '';
    this.email = qp.get('email') ?? '';
    if (this.reference && this.email) {
      this.find();
    }
  }

  find(): void {
    const errors: Record<string, string> = {};
    if (!this.reference.trim()) errors['reference'] = 'Please enter your booking reference.';
    if (!this.email.trim()) errors['email'] = 'Please enter the email used for the booking.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email.trim())) errors['email'] = 'Please enter a valid email address.';
    this.formErrors.set(errors);
    if (Object.keys(errors).length) return;

    this.searching.set(true);
    this.error.set(null);
    this.justCancelled.set(false);
    this.api.findBooking(this.reference, this.email).subscribe({
      next: (b) => {
        this.booking.set(b);
        this.searching.set(false);
      },
      error: (e) => {
        this.booking.set(null);
        this.error.set(errorMessage(e));
        this.searching.set(false);
      },
    });
  }

  askCancel(): void {
    this.cancelError.set(null);
    this.confirmOpen.set(true);
  }

  cancel(): void {
    const b = this.booking();
    if (!b || this.cancelling()) return;
    this.cancelling.set(true);
    this.api.cancelBooking(b.reference, this.email).subscribe({
      next: (updated) => {
        this.booking.set(updated);
        this.cancelling.set(false);
        this.confirmOpen.set(false);
        this.justCancelled.set(true);
      },
      error: (e) => {
        this.cancelError.set(errorMessage(e));
        this.cancelling.set(false);
      },
    });
  }

  reset(): void {
    this.booking.set(null);
    this.justCancelled.set(false);
    this.reference = '';
  }
}
