import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Staybook | Handpicked stays across India',
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'hotels',
    title: 'Hotels | Staybook',
    loadComponent: () => import('./pages/hotels/hotels.component').then((m) => m.HotelsComponent),
  },
  {
    path: 'hotels/:id',
    title: 'Hotel details | Staybook',
    loadComponent: () =>
      import('./pages/hotel-detail/hotel-detail.component').then((m) => m.HotelDetailComponent),
  },
  {
    path: 'confirmation/:reference',
    title: 'Booking confirmed | Staybook',
    loadComponent: () =>
      import('./pages/confirmation/confirmation.component').then((m) => m.ConfirmationComponent),
  },
  {
    path: 'manage-booking',
    title: 'Manage booking | Staybook',
    loadComponent: () =>
      import('./pages/manage-booking/manage-booking.component').then((m) => m.ManageBookingComponent),
  },
  {
    path: '**',
    title: 'Page not found | Staybook',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then((m) => m.NotFoundComponent),
  },
];
