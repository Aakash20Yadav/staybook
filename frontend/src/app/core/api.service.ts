import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  AlternativeStay,
  Booking,
  CreateBookingRequest,
  DayAvailability,
  HotelDetail,
  HotelSummary,
  Quote,
} from './models';

/** Thin wrapper around the Staybook REST API. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiBaseUrl;

  searchHotels(location?: string, checkIn?: string, checkOut?: string): Observable<HotelSummary[]> {
    return this.http.get<HotelSummary[]>(`${this.base}/hotels`, {
      params: this.params({ location, checkIn, checkOut }),
    });
  }

  locations(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/hotels/locations`);
  }

  hotel(id: number, checkIn?: string, checkOut?: string): Observable<HotelDetail> {
    return this.http.get<HotelDetail>(`${this.base}/hotels/${id}`, {
      params: this.params({ checkIn, checkOut }),
    });
  }

  calendar(id: number, from?: string, days = 30): Observable<DayAvailability[]> {
    return this.http.get<DayAvailability[]>(`${this.base}/hotels/${id}/calendar`, {
      params: this.params({ from, days: String(days) }),
    });
  }

  alternatives(id: number, checkIn: string, checkOut: string): Observable<AlternativeStay[]> {
    return this.http.get<AlternativeStay[]>(`${this.base}/hotels/${id}/alternatives`, {
      params: this.params({ checkIn, checkOut }),
    });
  }

  quote(hotelId: number, checkIn: string, checkOut: string): Observable<Quote> {
    return this.http.post<Quote>(`${this.base}/bookings/quote`, { hotelId, checkIn, checkOut });
  }

  createBooking(request: CreateBookingRequest): Observable<Booking> {
    return this.http.post<Booking>(`${this.base}/bookings`, request);
  }

  findBooking(reference: string, email: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.base}/bookings/${encodeURIComponent(reference.trim())}`, {
      params: this.params({ email: email.trim() }),
    });
  }

  cancelBooking(reference: string, email: string): Observable<Booking> {
    return this.http.post<Booking>(
      `${this.base}/bookings/${encodeURIComponent(reference.trim())}/cancel`,
      { email: email.trim() },
    );
  }

  private params(values: Record<string, string | undefined | null>): HttpParams {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(values)) {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, value);
      }
    }
    return params;
  }
}
