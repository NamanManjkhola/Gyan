import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
    documents: any;
  documentService: any;
    ngOnInit() {
      this.documentService.getDocuments().subscribe((data: any) => {
      this.documents = data;
    });
}
}
