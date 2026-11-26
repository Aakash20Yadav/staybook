import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { nightsBetween } from '../../core/dates';
import { errorMessage } from '../../core/errors';
import { HotelSummary } from '../../core/models';
import { HotelCardComponent } from '../../shared/hotel-card.component';
import { SearchFormComponent } from '../../shared/search-form.component';

type SortKey = 'recommended' | 'price-asc' | 'price-desc' | 'rating';

@Component({
  selector: 'app-hotels',
  imports: [FormsModule, DatePipe, RouterLink, SearchFormComponent, HotelCardComponent],
  templateUrl: './hotels.component.html',
  styleUrl: './hotels.component.scss',
})
export class HotelsComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly location = signal('');
  readonly checkIn = signal('');
  readonly checkOut = signal('');
  readonly formKey = signal(0);

  readonly hotels = signal<HotelSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly sort = signal<SortKey>('recommended');
  readonly onlyAvailable = signal(false);

  readonly hasDates = computed(() => !!this.checkIn() && !!this.checkOut());
  readonly nights = computed(() => (this.hasDates() ? nightsBetween(this.checkIn(), this.checkOut()) : 0));

  readonly visible = computed(() => {
    let list = [...this.hotels()];
    if (this.onlyAvailable() && this.hasDates()) {
      list = list.filter((h) => (h.availableRooms ?? 0) > 0);
    }
    switch (this.sort()) {
      case 'price-asc': list.sort((a, b) => a.pricePerNight - b.pricePerNight); break;
      case 'price-desc': list.sort((a, b) => b.pricePerNight - a.pricePerNight); break;
      case 'rating': list.sort((a, b) => b.starRating - a.starRating); break;
      default:
        // Available first, then by name (the API already returns name order).
        list.sort((a, b) => Number((b.availableRooms ?? 1) > 0) - Number((a.availableRooms ?? 1) > 0));
    }
    return list;
  });

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      this.location.set(params.get('location') ?? '');
      this.checkIn.set(params.get('checkIn') ?? '');
      this.checkOut.set(params.get('checkOut') ?? '');
      this.formKey.update((k) => k + 1);
      this.load();
    });
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api
      .searchHotels(this.location() || undefined, this.checkIn() || undefined, this.checkOut() || undefined)
      .subscribe({
        next: (list) => {
          this.hotels.set(list);
          this.loading.set(false);
        },
        error: (e) => {
          this.error.set(errorMessage(e));
          this.hotels.set([]);
          this.loading.set(false);
        },
      });
  }
}
