import { CurrencyPipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HotelSummary } from '../core/models';
import { ImgFallbackDirective } from './img-fallback.directive';

@Component({
  selector: 'app-hotel-card',
  imports: [RouterLink, CurrencyPipe, ImgFallbackDirective],
  template: `
    @let h = hotel();
    <a class="card" [routerLink]="['/hotels', h.id]" [queryParams]="dateParams()">
      <div class="media">
        <img [src]="h.coverImageUrl" [alt]="h.name" loading="lazy" appImgFallback />
        <span class="city">{{ h.city }}</span>
        @if (h.availableRooms !== null) {
          <span class="badge" [class]="'badge badge-' + availabilityClass()">{{ availabilityLabel() }}</span>
        }
      </div>
      <div class="body">
        <div class="stars" [attr.aria-label]="h.starRating + ' star hotel'">
          @for (s of stars(); track $index) { <span>★</span> }
        </div>
        <h3>{{ h.name }}</h3>
        <p class="room">{{ h.roomName }}</p>
        <ul class="amenities">
          @for (a of h.amenities.slice(0, 3); track a) { <li>{{ a }}</li> }
          @if (h.amenities.length > 3) { <li class="more">+{{ h.amenities.length - 3 }}</li> }
        </ul>
        <div class="foot">
          <div class="price">
            <strong>{{ h.pricePerNight | currency: h.currency : 'symbol' : '1.0-0' }}</strong>
            <span>/ night</span>
          </div>
          <span class="cta">View
            <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6" stroke="currentColor" stroke-width="2.2" fill="none"/></svg>
          </span>
        </div>
      </div>
    </a>
  `,
  styles: `
    :host { display: block; height: 100%; }
    .card {
      display: flex; flex-direction: column; height: 100%; background: var(--surface);
      border: 1px solid var(--line); color: var(--ink); text-decoration: none;
      transition: transform .35s cubic-bezier(.2,.7,.2,1), box-shadow .35s ease;
    }
    .card:hover { transform: translateY(-6px); box-shadow: 0 22px 40px rgba(27, 42, 47, .14); }
    .media { position: relative; aspect-ratio: 4 / 3; overflow: hidden; background: var(--forest); }
    .media img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform .8s cubic-bezier(.2,.7,.2,1); }
    .card:hover .media img { transform: scale(1.08); }
    .city {
      position: absolute; left: 0; bottom: 0; background: var(--ink); color: var(--paper);
      font-size: 12px; font-weight: 700; letter-spacing: .14em; text-transform: uppercase; padding: 8px 14px;
    }
    .badge { position: absolute; right: 12px; top: 12px; }
    .body { padding: 20px 22px 22px; display: flex; flex-direction: column; flex: 1; }
    .stars { color: var(--gold); font-size: 13px; letter-spacing: 2px; }
    h3 { font-family: var(--font-display); font-size: 22px; font-weight: 600; margin: 6px 0 4px; line-height: 1.2; }
    .room { color: var(--muted); font-size: 14px; margin: 0 0 14px; }
    .amenities { list-style: none; padding: 0; margin: 0 0 18px; display: flex; flex-wrap: wrap; gap: 6px; }
    .amenities li { font-size: 12px; padding: 5px 10px; background: var(--paper); color: var(--ink); }
    .amenities .more { background: var(--ink); color: var(--paper); }
    .foot { margin-top: auto; display: flex; align-items: flex-end; justify-content: space-between; padding-top: 14px; border-bottom: 0; }
    .price strong { font-size: 22px; font-weight: 800; }
    .price span { color: var(--muted); font-size: 13px; margin-left: 4px; }
    .cta { display: inline-flex; gap: 6px; align-items: center; font-weight: 700; color: var(--accent); font-size: 14px; }
    .cta svg { transition: transform .25s ease; }
    .card:hover .cta svg { transform: translateX(5px); }
  `,
})
export class HotelCardComponent {
  readonly hotel = input.required<HotelSummary>();
  readonly checkIn = input<string | null>(null);
  readonly checkOut = input<string | null>(null);

  readonly stars = computed(() => Array.from({ length: this.hotel().starRating }));
  readonly dateParams = computed(() =>
    this.checkIn() && this.checkOut() ? { checkIn: this.checkIn(), checkOut: this.checkOut() } : {},
  );
  readonly availabilityClass = computed(() => {
    const left = this.hotel().availableRooms ?? 0;
    if (left === 0) return 'soldout';
    return left <= Math.max(1, Math.ceil(this.hotel().totalRooms * 0.3)) ? 'limited' : 'available';
  });
  readonly availabilityLabel = computed(() => {
    const left = this.hotel().availableRooms ?? 0;
    if (left === 0) return 'Sold out';
    return left === 1 ? 'Last room' : `${left} rooms left`;
  });
}
