import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, ElementRef, OnInit, computed, inject, input, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { addDays, nightsBetween, todayIso, validateStay } from '../../core/dates';
import { errorCode, errorMessage, fieldErrors } from '../../core/errors';
import { AlternativeStay, DayAvailability, HotelDetail, Quote } from '../../core/models';
import { ImgFallbackDirective } from '../../shared/img-fallback.directive';
import { RevealDirective } from '../../shared/reveal.directive';

import { LAST_BOOKING_KEY } from '../../core/storage';
const CALENDAR_PAGE = 30;
const CALENDAR_MAX_DAYS = 90;

@Component({
  selector: 'app-hotel-detail',
  imports: [FormsModule, CurrencyPipe, DatePipe, RouterLink, ImgFallbackDirective, RevealDirective],
  templateUrl: './hotel-detail.component.html',
  styleUrl: './hotel-detail.component.scss',
})
export class HotelDetailComponent implements OnInit {
  /** Bound from the route path /hotels/:id */
  readonly id = input.required<string>();

  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly today = todayIso();

  // ---- hotel ----
  readonly hotel = signal<HotelDetail | null>(null);
  readonly loading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly gallery = computed(() => {
    const h = this.hotel();
    return h ? (h.images.length ? h.images : [h.coverImageUrl]) : [];
  });
  readonly slide = signal(0);
  private touchStartX = 0;

  // ---- dates & quote ----
  checkIn = '';
  checkOut = '';
  readonly dateError = signal<string | null>(null);
  readonly quote = signal<Quote | null>(null);
  readonly quoting = signal(false);
  readonly quoteError = signal<string | null>(null);
  readonly alternatives = signal<AlternativeStay[]>([]);

  // ---- availability calendar (surprise feature) ----
  readonly days = signal<DayAvailability[]>([]);
  readonly calendarLoading = signal(false);
  readonly pickingCheckOut = signal(false);
  readonly canLoadMoreDays = computed(() => this.days().length < CALENDAR_MAX_DAYS);
  private readonly strip = viewChild<ElementRef<HTMLElement>>('strip');

  // ---- booking drawer ----
  readonly drawerOpen = signal(false);
  /** Snapshot of the price the guest agreed to when opening the drawer. */
  readonly drawerQuote = signal<Quote | null>(null);
  guestName = '';
  guestEmail = '';
  readonly formErrors = signal<Record<string, string>>({});
  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);

  readonly nights = computed(() => this.quote()?.nights ?? 0);

  ngOnInit(): void {
    const qp = this.route.snapshot.queryParamMap;
    this.checkIn = qp.get('checkIn') ?? '';
    this.checkOut = qp.get('checkOut') ?? '';

    this.api.hotel(+this.id()).subscribe({
      next: (h) => {
        this.hotel.set(h);
        this.loading.set(false);
        this.loadCalendar();
        if (this.checkIn && this.checkOut) {
          this.refreshQuote();
        }
      },
      error: (e) => {
        this.loadError.set(errorMessage(e));
        this.loading.set(false);
      },
    });
  }

  // ================= Gallery =================
  goTo(index: number): void {
    const n = this.gallery().length;
    this.slide.set((index + n) % n);
  }

  onTouchStart(e: TouchEvent): void {
    this.touchStartX = e.changedTouches[0].clientX;
  }

  onTouchEnd(e: TouchEvent): void {
    const dx = e.changedTouches[0].clientX - this.touchStartX;
    if (Math.abs(dx) > 40) {
      this.goTo(this.slide() + (dx < 0 ? 1 : -1));
    }
  }

  // ================= Dates & quote =================
  onCheckInChange(): void {
    if (this.checkIn && (!this.checkOut || this.checkOut <= this.checkIn)) {
      this.checkOut = addDays(this.checkIn, 1);
    }
    this.pickingCheckOut.set(false);
    this.refreshQuote();
  }

  refreshQuote(): void {
    const h = this.hotel();
    if (!h) return;
    const problem = validateStay(this.checkIn, this.checkOut);
    this.dateError.set(problem);
    this.quote.set(null);
    this.alternatives.set([]);
    this.quoteError.set(null);
    if (problem) return;

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { checkIn: this.checkIn, checkOut: this.checkOut },
      replaceUrl: true,
    });

    this.quoting.set(true);
    this.api.quote(h.id, this.checkIn, this.checkOut).subscribe({
      next: (q) => {
        this.quote.set(q);
        this.quoting.set(false);
        if (!q.available) {
          this.loadAlternatives();
        }
      },
      error: (e) => {
        this.quoteError.set(errorMessage(e));
        this.quoting.set(false);
      },
    });
  }

  private loadAlternatives(): void {
    const h = this.hotel();
    if (!h) return;
    this.api.alternatives(h.id, this.checkIn, this.checkOut).subscribe({
      next: (a) => this.alternatives.set(a),
      error: () => this.alternatives.set([]),
    });
  }

  applyAlternative(a: AlternativeStay): void {
    this.checkIn = a.checkIn;
    this.checkOut = a.checkOut;
    this.refreshQuote();
  }

  shiftLabel(days: number): string {
    const n = Math.abs(days);
    return `${n} ${n === 1 ? 'day' : 'days'} ${days > 0 ? 'later' : 'earlier'}`;
  }

  // ================= Calendar (surprise feature) =================
  loadCalendar(append = false): void {
    const h = this.hotel();
    if (!h) return;
    const current = this.days();
    const from = append && current.length ? addDays(current[current.length - 1].date, 1) : this.today;
    this.calendarLoading.set(true);
    this.api.calendar(h.id, from, CALENDAR_PAGE).subscribe({
      next: (d) => {
        this.days.set(append ? [...current, ...d] : d);
        this.calendarLoading.set(false);
        if (append) {
          setTimeout(() => this.scrollStrip(1));
        }
      },
      error: () => this.calendarLoading.set(false),
    });
  }

  /** Click once for check-in, again for check-out. */
  pickDay(day: DayAvailability): void {
    if (!this.pickingCheckOut()) {
      if (day.level === 'SOLD_OUT') return;
      this.checkIn = day.date;
      this.checkOut = addDays(day.date, 1);
      this.pickingCheckOut.set(true);
      this.refreshQuote();
      return;
    }
    if (day.date <= this.checkIn) {
      // Clicked on or before check-in: start over from this day.
      this.pickingCheckOut.set(false);
      this.pickDay(day);
      return;
    }
    this.checkOut = day.date;
    this.pickingCheckOut.set(false);
    this.refreshQuote();
  }

  inRange(date: string): boolean {
    return !!this.checkIn && !!this.checkOut && date >= this.checkIn && date < this.checkOut;
  }

  isCheckout(date: string): boolean {
    return date === this.checkOut;
  }

  scrollStrip(direction: 1 | -1): void {
    const el = this.strip()?.nativeElement;
    el?.scrollBy({ left: direction * el.clientWidth * 0.8, behavior: 'smooth' });
  }

  isFirstOfMonth(date: string, index: number): boolean {
    return index === 0 || date.endsWith('-01');
  }

  // ================= Booking =================
  openDrawer(): void {
    if (!this.quote()?.available) return;
    this.drawerQuote.set(this.quote());
    this.submitError.set(null);
    this.formErrors.set({});
    this.drawerOpen.set(true);
    document.body.style.overflow = 'hidden';
  }

  closeDrawer(): void {
    this.drawerOpen.set(false);
    document.body.style.overflow = '';
  }

  private validateGuest(): boolean {
    const errors: Record<string, string> = {};
    const name = this.guestName.trim();
    if (!name) errors['guestName'] = 'Please enter your full name.';
    else if (name.length < 2) errors['guestName'] = 'Name must be at least 2 characters.';
    else if (!/^[\p{L} .'-]+$/u.test(name)) errors['guestName'] = 'Name can contain letters, spaces, apostrophes, dots and hyphens only.';
    const email = this.guestEmail.trim();
    if (!email) errors['guestEmail'] = 'Please enter your email address.';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) errors['guestEmail'] = 'Please enter a valid email address.';
    this.formErrors.set(errors);
    return Object.keys(errors).length === 0;
  }

  confirm(): void {
    const h = this.hotel();
    if (!h || this.submitting() || !this.validateGuest()) return;
    this.submitting.set(true);
    this.submitError.set(null);

    this.api
      .createBooking({
        hotelId: h.id,
        checkIn: this.checkIn,
        checkOut: this.checkOut,
        guestName: this.guestName.trim(),
        guestEmail: this.guestEmail.trim(),
      })
      .subscribe({
        next: (booking) => {
          sessionStorage.setItem(LAST_BOOKING_KEY, JSON.stringify(booking));
          this.closeDrawer();
          this.router.navigate(['/confirmation', booking.reference], { state: { booking } });
        },
        error: (e) => {
          this.submitting.set(false);
          this.formErrors.set(fieldErrors(e));
          this.submitError.set(errorMessage(e));
          if (errorCode(e) === 'ROOM_UNAVAILABLE') {
            // Someone else took the last room: refresh what the guest sees.
            this.loadCalendar();
            this.refreshQuote();
          }
        },
      });
  }

  starsOf(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i);
  }

  nightsLabel(n: number): string {
    return `${n} ${n === 1 ? 'night' : 'nights'}`;
  }

  protected readonly nightsBetween = nightsBetween;
}
