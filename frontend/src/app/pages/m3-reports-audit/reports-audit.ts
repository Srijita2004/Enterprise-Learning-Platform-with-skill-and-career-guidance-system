import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { finalize, forkJoin } from 'rxjs';
import { CertificationApiService } from '../../core/api/certification-api.service';
import { apiErrorMessage } from '../../core/http-error';
import { ToastService } from '../../core/toast/toast.service';
import { AuditLog, ReportSummary } from '../../models/certification.models';
import { DonutChartComponent, DonutSegment } from '../../shared/donut-chart/donut-chart';
import { humanizeStatus } from '../../shared/status-label';

@Component({selector:'app-reports-audit-page',standalone:true,imports:[CommonModule,DonutChartComponent],templateUrl:'./reports-audit.html',styleUrl:'./reports-audit.css'})
export class ReportsAuditPage {
  private readonly api=inject(CertificationApiService); private readonly toast=inject(ToastService);
  readonly summary=signal<ReportSummary|null>(null); readonly audit=signal<AuditLog[]>([]); readonly loading=signal(true); readonly humanize=humanizeStatus;
  constructor(){this.load();}
  load():void{this.loading.set(true);forkJoin({summary:this.api.reportSummary(),audit:this.api.audit()}).pipe(finalize(()=>this.loading.set(false))).subscribe({next:r=>{this.summary.set(r.summary);this.audit.set(r.audit);},error:e=>this.toast.error(apiErrorMessage(e,'Unable to load reports and audit data.'))});}
  renewalSegments(s:ReportSummary):DonutSegment[]{return[{label:'Completed',value:s.completedRenewals,color:'#22c55e'},{label:'Rejected',value:s.rejectedRenewals,color:'#ef4444'}];}
  downloadAll():void{this.api.downloadCertificationCsv().subscribe({next:r=>this.saveBlob(r.body,'certification-report.csv'),error:e=>this.toast.error(apiErrorMessage(e,'Report download failed.'))});}
  downloadExpiring():void{this.api.downloadExpiringCsv(30).subscribe({next:r=>this.saveBlob(r.body,'expiring-certifications-30-days.csv'),error:e=>this.toast.error(apiErrorMessage(e,'Expiring report download failed.'))});}
  private saveBlob(blob:Blob|null,name:string):void{if(!blob){this.toast.error('The report response was empty.');return;}const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download=name;a.click();URL.revokeObjectURL(url);this.toast.success(`${name} downloaded.`);}
}
