import { Directive, ElementRef, HostListener, inject } from '@angular/core';

const FALLBACK =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 520">
      <rect width="800" height="520" fill="#2F5D50"/>
      <rect x="360" y="200" width="80" height="80" fill="none" stroke="#F6F1E9" stroke-width="6"/>
      <path d="M360 280 L400 236 L440 280" fill="none" stroke="#F6F1E9" stroke-width="6"/>
      <text x="400" y="330" text-anchor="middle" font-family="Georgia, serif" font-size="26" fill="#F6F1E9">Staybook</text>
    </svg>`,
  );

/** Swaps a broken image for a solid-colour placeholder so layouts never show a broken icon. */
@Directive({ selector: 'img[appImgFallback]' })
export class ImgFallbackDirective {
  private readonly el = inject<ElementRef<HTMLImageElement>>(ElementRef);

  @HostListener('error')
  onError(): void {
    const img = this.el.nativeElement;
    if (img.src !== FALLBACK) {
      img.src = FALLBACK;
      img.classList.add('img-fallback');
    }
  }
}
