const _elementMargin = 10;
const _elementInitialWidth = 200;
const _elementInitialHeight = 200;
const _canvasWidth = 1024;

export class PositionUtils {

    static _doOverlap(positionElement1, positionElement2) {
        // https://stackoverflow.com/a/12067046
        return !(
            (positionElement1.x + positionElement1.width) < positionElement2.x ||
            positionElement1.x > (positionElement2.x + positionElement2.width) ||
            (positionElement1.y + positionElement1.height) < positionElement2.y ||
            positionElement1.y > (positionElement2.y + positionElement2.height)
        )
    }

    static _getNextPossiblePosition(suggestedPosition, existingElement){
        let temp = {
            x: suggestedPosition.x,
            y: suggestedPosition.y,
            width: suggestedPosition.width,
            height: suggestedPosition.height
        };
        temp.x = existingElement.x + existingElement.width + _elementMargin;
        if(temp.x + _elementInitialWidth > _canvasWidth) {
            temp.x = _elementMargin;
            temp.y = existingElement.y + existingElement.height + _elementMargin;
        }
        return temp;
    }

    static findNewPosition(suggestedPosition, existingElements) {
        if(! suggestedPosition) {
            suggestedPosition = {
                x: _elementMargin,
                y: _elementMargin,
                width: _elementInitialWidth,
                height: _elementInitialHeight
            }
        }
        for (const existingElement of existingElements) {
            if(this._doOverlap(suggestedPosition, existingElement.position)){
                let nextPossiblePosition = this._getNextPossiblePosition(suggestedPosition, existingElement.position)
                return this.findNewPosition(nextPossiblePosition, existingElements);
            }
        }
        return {
            x: suggestedPosition.x,
            y: suggestedPosition.y,
            height: _elementInitialHeight,
            width: _elementInitialWidth};
    }
}
