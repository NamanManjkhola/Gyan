import { Component } from '@angular/core';

@Component({
  selector: 'app-upload',
  imports: [],
  templateUrl: './upload.html',
  styleUrl: './upload.scss',
})
export class Upload {
    documentService: any;
    onFileSelected(event: any) {
      const file = event.target.files[0];
      this.documentService.upload(file).subscribe();
   }
}
