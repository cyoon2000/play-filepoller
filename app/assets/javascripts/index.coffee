$ ->
  $.get "/listFileEntries", (data) ->
    $.each data, (index, item) ->
      $("#fileEntries").append "<tr>
         <td>" + item.name + "</td>
         <td>" + item.signature + "</td>
         <td>" + item.size + "</td>
         <td>" + formatDateLocal(new Date(item.lastModified)) + "</td>
         <td>" + item.root + "</td></tr>"


# returns UTC time
formatDate = (date) ->
  console.log date
  date.toISOString().replace /\..+$|[^\d]/g, ''

# returns local time
formatDateLocal = (date) ->
  normalisedDate = new Date(date - (date.getTimezoneOffset() * 60 * 1000))
  normalisedDate.toISOString().replace /\..+$|[^\d]/g, ''





