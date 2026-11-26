export interface HotelSummary {
  id: number;
  name: string;
  city: string;
  address: string;
  description: string;
  starRating: number;
  coverImageUrl: string;
  amenities: string[];
  roomName: string;
  pricePerNight: number;
  currency: string;
  totalRooms: number;
  availableRooms: number | null;
}

export interface Room {
  id: number;
  name: string;
  description: string;
  bedType: string;
  maxGuests: number;
  pricePerNight: number;
  totalRooms: number;
}

export interface StayAvailability {
  checkIn: string;
  checkOut: string;
  nights: number;
  availableRooms: number;
  available: boolean;
  totalAmount: number;
}

export interface HotelDetail {
  id: number;
  name: string;
  city: string;
  address: string;
  description: string;
  starRating: number;
  coverImageUrl: string;
  images: string[];
  amenities: string[];
  currency: string;
  room: Room;
  stay: StayAvailability | null;
}

export interface Quote {
  hotelId: number;
  hotelName: string;
  roomName: string;
  checkIn: string;
  checkOut: string;
  nights: number;
  pricePerNight: number;
  totalAmount: number;
  currency: string;
  availableRooms: number;
  available: boolean;
}

export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface Booking {
  reference: string;
  status: BookingStatus;
  hotelId: number;
  hotelName: string;
  hotelCity: string;
  hotelAddress: string;
  coverImageUrl: string;
  roomName: string;
  guestName: string;
  guestEmail: string;
  checkIn: string;
  checkOut: string;
  nights: number;
  pricePerNight: number;
  totalAmount: number;
  currency: string;
  createdAt: string;
  cancelledAt: string | null;
  cancellable: boolean;
}

export interface CreateBookingRequest {
  hotelId: number;
  checkIn: string;
  checkOut: string;
  guestName: string;
  guestEmail: string;
}

export type DayLevel = 'AVAILABLE' | 'LIMITED' | 'SOLD_OUT';

export interface DayAvailability {
  date: string;
  availableRooms: number;
  totalRooms: number;
  level: DayLevel;
  pricePerNight: number;
}

export interface AlternativeStay {
  checkIn: string;
  checkOut: string;
  nights: number;
  shiftDays: number;
  availableRooms: number;
  totalAmount: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
  fieldErrors: { field: string; message: string }[];
}
