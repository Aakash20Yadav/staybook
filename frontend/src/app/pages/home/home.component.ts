import { Component, ElementRef, OnDestroy, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { errorMessage } from '../../core/errors';
import { HotelSummary } from '../../core/models';
import { HotelCardComponent } from '../../shared/hotel-card.component';
import { ImgFallbackDirective } from '../../shared/img-fallback.directive';
import { RevealDirective } from '../../shared/reveal.directive';
import { SearchFormComponent } from '../../shared/search-form.component';

interface Slide {
  image: string;
  place: string;
  title: string;
  text: string;
}

const IMG = (id: string) => `https://images.unsplash.com/photo-${id}?auto=format&fit=crop&w=1920&q=80`;

@Component({
  selector: 'app-home',
  imports: [RouterLink, SearchFormComponent, HotelCardComponent, RevealDirective, ImgFallbackDirective],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);

  readonly slides: Slide[] = [
    { image: IMG('1520250497591-112f2f40a3f4'), place: 'Candolim, Goa', title: 'Wake up to the sound of the sea',
      text: 'Palm-shaded cottages two minutes from the sand.' },
    { image: IMG('1571896349842-33c89424de2d'), place: 'Lake Pichola, Udaipur', title: 'Palaces, lakes and slow evenings',
      text: 'Suites that open onto the water and the City Palace.' },
    { image: IMG('1566073771259-6a8506099945'), place: 'Pink City, Jaipur', title: 'Sleep inside a little history',
      text: 'A restored haveli a short walk from the Hawa Mahal.' },
    { image: IMG('1445019980597-93fa8acb246c'), place: 'Old Manali', title: 'Snow peaks from your balcony',
      text: 'Timber lodges, bonfires and trails at the door.' },
  ];
  readonly current = signal(0);
  private timer?: ReturnType<typeof setInterval>;

  readonly hotels = signal<HotelSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  /** Number of distinct cities. */
  readonly cityCount = computed(() => new Set(this.hotels().map((h) => h.city)).size);

  /** One card per city, using that city's first hotel image. */
  readonly destinations = computed(() => {
    const seen = new Map<string, { city: string; image: string; count: number; from: number }>();
    for (const h of this.hotels()) {
      const d = seen.get(h.city);
      if (d) {
        d.count++;
        d.from = Math.min(d.from, h.pricePerNight);
      } else {
        seen.set(h.city, { city: h.city, image: h.coverImageUrl, count: 1, from: h.pricePerNight });
      }
    }
    return [...seen.values()];
  });

  private readonly track = viewChild<ElementRef<HTMLElement>>('track');
  readonly canPrev = signal(false);
  readonly canNext = signal(true);

  ngOnInit(): void {
    this.startAutoplay();
    this.api.searchHotels().subscribe({
      next: (h) => {
        this.hotels.set(h);
        this.loading.set(false);
        setTimeout(() => this.updateArrows());
      },
      error: (e) => {
        this.error.set(errorMessage(e));
        this.loading.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  go(index: number): void {
    const n = this.slides.length;
    this.current.set((index + n) % n);
    this.startAutoplay();
  }

  private startAutoplay(): void {
    clearInterval(this.timer);
    this.timer = setInterval(() => this.current.set((this.current() + 1) % this.slides.length), 6000);
  }

  scrollTrack(direction: 1 | -1): void {
    const el = this.track()?.nativeElement;
    if (!el) return;
    const card = el.querySelector<HTMLElement>('.slide-card');
    const step = card ? card.offsetWidth + 24 : el.clientWidth * 0.8;
    el.scrollBy({ left: direction * step, behavior: 'smooth' });
  }

  updateArrows(): void {
    const el = this.track()?.nativeElement;
    if (!el) return;
    this.canPrev.set(el.scrollLeft > 4);
    this.canNext.set(el.scrollLeft + el.clientWidth < el.scrollWidth - 4);
  }
}
