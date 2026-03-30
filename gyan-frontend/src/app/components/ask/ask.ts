import { Component } from '@angular/core';

@Component({
  selector: 'app-ask',
  imports: [],
  templateUrl: './ask.html',
  styleUrl: './ask.scss',
})
export class Ask {
    documentService: any;
  answer: any;
  question: any;
    ask() {
      this.documentService.ask(this.question).subscribe((res: any) => {
      this.answer = res;
    });
}

}
