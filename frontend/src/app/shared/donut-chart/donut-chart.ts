import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

export interface DonutSegment { label: string; value: number; color: string; }
interface RenderSegment extends DonutSegment { percent: number; offset: number; }

@Component({
  selector: 'app-donut-chart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './donut-chart.html',
  styleUrl: './donut-chart.css'
})
export class DonutChartComponent {
  @Input() title = 'Distribution';
  @Input() centerLabel = 'Total';
  @Input() segments: DonutSegment[] = [];

  get total(): number { return this.segments.reduce((sum, segment) => sum + segment.value, 0); }
  get rendered(): RenderSegment[] {
    const total = this.total;
    let offset = 0;
    return this.segments.map(segment => {
      const percent = total > 0 ? (segment.value / total) * 100 : 0;
      const rendered = { ...segment, percent, offset };
      offset += percent;
      return rendered;
    });
  }
  percent(value: number): string { return this.total ? `${Math.round(value * 100 / this.total)}%` : '0%'; }
}
